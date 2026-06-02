package com.anddd.nevera.infra.ai.engine

import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.usecase.ai.GetGemmaModelPathUseCase
import com.anddd.nevera.infra.ai.image.GemmaImageNormalizer
import com.anddd.nevera.infra.ai.performance.GemmaImageAnalysisTrace
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class LiteRtGemmaInferenceEngine(
    private val getGemmaModelPath: GetGemmaModelPathUseCase,
    private val imageNormalizer: GemmaImageNormalizer,
    private val cacheDir: java.io.File,
) : GemmaInferenceEngine {

    private val mutex = Mutex()
    private val inferenceMutex = Mutex()
    private var engine: Engine? = null
    private var initializedModelPath: String? = null

    override fun generateText(request: GemmaPromptRequest): Flow<GemmaGenerationEvent> = flow {
        inferenceMutex.withLock {
            if (request.prompt.isBlank()) {
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EmptyPrompt))
                return@withLock
            }

            val modelPath = getGemmaModelPath() ?: run {
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ModelNotReady))
                return@withLock
            }

            val conversation = try {
                val eng = ensureEngine(modelPath)
                val systemContents = request.systemPrompt?.let { Contents.of(Content.Text(it)) }
                val config = ConversationConfig(systemInstruction = systemContents ?: Contents.of())
                eng.createConversation(config)
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                Timber.e(e, "Text generation ran out of memory")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.Unknown(e)))
                return@withLock
            } catch (e: Exception) {
                Timber.e(e, "Engine initialize failed")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EngineInitializeFailed))
                return@withLock
            }

            conversation.use {
                emit(GemmaGenerationEvent.Started)
                val fullText = StringBuilder()
                try {
                    conversation.sendMessageAsync(request.prompt).collect { message ->
                        val token = message.contents.contents
                            .filterIsInstance<Content.Text>()
                            .joinToString("") { it.text }
                        if (token.isNotEmpty()) {
                            fullText.append(token)
                            emit(GemmaGenerationEvent.Token(token))
                        }
                    }
                    emit(GemmaGenerationEvent.Completed(fullText.toString()))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Text generation failed")
                    emit(GemmaGenerationEvent.Failed(GemmaGenerationError.GenerationFailed))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun analyzeImage(request: GemmaImagePromptRequest): Flow<GemmaGenerationEvent> = flow {
        inferenceMutex.withLock {
            val trace = request.traceId?.let(::GemmaImageAnalysisTrace) ?: GemmaImageAnalysisTrace()
            if (request.prompt.isBlank()) {
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EmptyPrompt))
                return@withLock
            }

            val modelPath = getGemmaModelPath() ?: run {
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ModelNotReady))
                return@withLock
            }

            val analysisPrompt = trace.measure("promptBuild") {
                buildAnalysisPrompt(request.prompt)
            }
            trace.value("promptChars", analysisPrompt.length)
            trace.value("maxTokensRequested", request.maxTokens)

            val normalizedFile = try {
                imageNormalizer.normalize(request.imageUri, trace)
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                Timber.e(e, "Image normalization ran out of memory: ${request.imageUri}")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageNormalizeFailed))
                return@withLock
            } catch (e: Exception) {
                Timber.e(e, "Image read failed: ${request.imageUri}")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageReadFailed))
                return@withLock
            } ?: run {
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageReadFailed))
                return@withLock
            }

            if (!normalizedFile.exists() || normalizedFile.length() <= 0L) {
                Timber.e(
                    "Normalized image file is invalid: path=%s, exists=%s, size=%s",
                    normalizedFile.absolutePath,
                    normalizedFile.exists(),
                    normalizedFile.length(),
                )
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageNormalizeFailed))
                return@withLock
            }

            Timber.d(
                "Normalized image ready: path=%s, size=%d bytes",
                normalizedFile.absolutePath,
                normalizedFile.length(),
            )

            val conversation = try {
                val eng = ensureEngine(modelPath, trace)
                val systemContents = request.systemPrompt?.let { Contents.of(Content.Text(it)) }
                val config = ConversationConfig(
                    systemInstruction = systemContents ?: Contents.of(),
                    samplerConfig = IMAGE_ANALYSIS_SAMPLER_CONFIG,
                )
                trace.value("sampler", "temperature=0.0 topK=1 topP=1.0")
                trace.measure("conversationCreate") {
                    eng.createConversation(config)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                Timber.e(e, "Engine initialize ran out of memory")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.Unknown(e)))
                return@withLock
            } catch (e: Exception) {
                Timber.e(e, "Engine initialize failed")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EngineInitializeFailed))
                return@withLock
            }

            val userContents = Contents.of(
                Content.ImageFile(normalizedFile.absolutePath),
                Content.Text(analysisPrompt),
            )

            conversation.use {
                emit(GemmaGenerationEvent.Started)
                try {
                    val message = trace.measure("inference") {
                        conversation.sendMessage(userContents)
                    }
                    val fullText = trace.measure("responseParse") {
                        message.extractText()
                    }
                    emit(GemmaGenerationEvent.Completed(fullText))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: OutOfMemoryError) {
                    Timber.e(e, "Image analysis ran out of memory")
                    emit(GemmaGenerationEvent.Failed(GemmaGenerationError.Unknown(e)))
                } catch (e: Exception) {
                    Timber.e(e, "Image analysis failed")
                    emit(GemmaGenerationEvent.Failed(GemmaGenerationError.GenerationFailed))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun close() {
        inferenceMutex.withLock {
            mutex.withLock {
                engine?.close()
                engine = null
                initializedModelPath = null
            }
        }
    }

    private suspend fun ensureEngine(
        modelPath: String,
        trace: GemmaImageAnalysisTrace? = null,
    ): Engine = mutex.withLock {
        val current = engine
        if (current != null && initializedModelPath == modelPath) {
            trace?.record("modelInitialize", 0L, "reused=true backend=CPU visionBackend=CPU")
            return@withLock current
        }

        current?.close()
        engine = null
        initializedModelPath = null

        withContext(Dispatchers.IO) {
            trace?.value("backend", "CPU")
            trace?.value("visionBackend", "CPU")
            if (trace == null) {
                createEngine(modelPath)
            } else {
                trace.measure("modelInitialize") {
                    createEngine(modelPath)
                }
            }
        }
    }

    private fun createEngine(modelPath: String): Engine {
        val engineCacheDir = cacheDir.resolve("gemma4_litertlm_cache").also { it.mkdirs() }
        val config = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU(),
            visionBackend = Backend.CPU(),
            audioBackend = Backend.CPU(),
            cacheDir = engineCacheDir.absolutePath,
        )
        return Engine(config).also { eng ->
            eng.initialize()
            engine = eng
            initializedModelPath = modelPath
        }
    }

    private fun buildAnalysisPrompt(userPrompt: String): String = """
        영수증 이미지에서 식재료 구매 항목만 추출해 JSON 객체만 반환해. 설명/마크다운/주석 금지.
        형식: {"ocrText":string|null,"contextSummary":string|null,"ingredients":[{"name":string,"quantityText":string|null,"categoryText":string|null,"storageHint":string|null,"confidence":number|null}]}
        규칙: 결제금액/합계/부가세/할인/포인트/카드번호/승인번호/매장주소/전화번호는 제외. 식재료가 없으면 ingredients=[].
        confidence는 0.0~1.0 범위. 사용자 요청: $userPrompt
    """.trimIndent()

    private fun Message.extractText(): String =
        contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString("") { it.text }

    private companion object {
        val IMAGE_ANALYSIS_SAMPLER_CONFIG = SamplerConfig(
            topK = 1,
            topP = 1.0,
            temperature = 0.0,
        )
    }
}
