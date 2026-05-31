package com.anddd.nevera.infra.ai.engine

import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.usecase.ai.GetGemmaModelPathUseCase
import com.anddd.nevera.infra.ai.image.GemmaImageNormalizer
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
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
    private var engine: Engine? = null
    private var initializedModelPath: String? = null

    override fun generateText(request: GemmaPromptRequest): Flow<GemmaGenerationEvent> = flow {
        if (request.prompt.isBlank()) {
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EmptyPrompt))
            return@flow
        }

        val modelPath = getGemmaModelPath() ?: run {
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ModelNotReady))
            return@flow
        }

        val conversation = try {
            val eng = ensureEngine(modelPath)
            val systemContents = request.systemPrompt?.let { Contents.of(Content.Text(it)) }
            val config = ConversationConfig(systemInstruction = systemContents ?: Contents.of())
            eng.createConversation(config)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Engine initialize failed")
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EngineInitializeFailed))
            return@flow
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
    }.flowOn(Dispatchers.IO)

    override fun analyzeImage(request: GemmaImagePromptRequest): Flow<GemmaGenerationEvent> = flow {
        if (request.prompt.isBlank()) {
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EmptyPrompt))
            return@flow
        }

        val modelPath = getGemmaModelPath() ?: run {
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ModelNotReady))
            return@flow
        }

        val normalizedFile = try {
            imageNormalizer.normalize(request.imageUri)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Image read failed: ${request.imageUri}")
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageReadFailed))
            return@flow
        } ?: run {
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.ImageReadFailed))
            return@flow
        }

        val conversation = try {
            val eng = ensureEngine(modelPath)
            val systemContents = request.systemPrompt?.let { Contents.of(Content.Text(it)) }
            val config = ConversationConfig(systemInstruction = systemContents ?: Contents.of())
            eng.createConversation(config)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Engine initialize failed")
            emit(GemmaGenerationEvent.Failed(GemmaGenerationError.EngineInitializeFailed))
            return@flow
        }

        val analysisPrompt = buildAnalysisPrompt(request.prompt)
        val userContents = Contents.of(
            Content.ImageFile(normalizedFile.absolutePath),
            Content.Text(analysisPrompt),
        )

        conversation.use {
            emit(GemmaGenerationEvent.Started)
            val fullText = StringBuilder()
            try {
                conversation.sendMessageAsync(userContents).collect { message ->
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
                Timber.e(e, "Image analysis failed")
                emit(GemmaGenerationEvent.Failed(GemmaGenerationError.GenerationFailed))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun close() {
        mutex.withLock {
            engine?.close()
            engine = null
            initializedModelPath = null
        }
    }

    private suspend fun ensureEngine(modelPath: String): Engine = mutex.withLock {
        val current = engine
        if (current != null && initializedModelPath == modelPath) return@withLock current

        current?.close()
        engine = null
        initializedModelPath = null

        withContext(Dispatchers.IO) {
            val engineCacheDir = cacheDir.resolve("gemma4_litertlm_cache").also { it.mkdirs() }
            val config = EngineConfig(
                modelPath = modelPath,
                cacheDir = engineCacheDir.absolutePath,
            )
            Engine(config).also { eng ->
                eng.initialize()
                engine = eng
                initializedModelPath = modelPath
            }
        }
    }

    private fun buildAnalysisPrompt(userPrompt: String): String = """
        이미지에서 보이는 텍스트를 OCR하고, 영수증/식재료 문맥을 분석해 JSON만 반환해.
        설명 문장, markdown code fence, 주석은 출력하지 마.

        JSON schema:
        {
          "ocrText": "이미지에서 읽은 전체 텍스트. 없으면 null",
          "contextSummary": "이미지/프롬프트를 종합한 짧은 한국어 요약. 없으면 null",
          "ingredients": [
            {
              "name": "식재료명",
              "quantityText": "수량 원문. 없으면 null",
              "categoryText": "카테고리 추정. 없으면 null",
              "storageHint": "냉장/냉동/실온 등 보관 힌트. 없으면 null",
              "confidence": 0.0
            }
          ]
        }

        사용자 요청:
        $userPrompt
    """.trimIndent()
}
