package com.anddd.nevera.infra.ai.parser

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult
import com.anddd.nevera.domain.model.ai.GemmaAnalyzedIngredient
import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject

internal class GemmaAnalysisResponseParser @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(rawText: String): NeveraResult<GemmaAnalysisResult, GemmaGenerationError> {
        return try {
            val cleaned = removeFences(rawText)
            val jsonCandidate = extractJsonObject(cleaned) ?: return NeveraResult.Failure(
                GemmaGenerationError.ResponseParseFailed,
            )

            val root = json.parseToJsonElement(jsonCandidate).jsonObject
            NeveraResult.Success(parseResult(rawText, root))
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse Gemma analysis response")
            NeveraResult.Failure(GemmaGenerationError.ResponseParseFailed)
        }
    }

    private fun removeFences(text: String): String =
        text.replace(Regex("```[a-zA-Z]*\\s*"), "").replace("```", "")

    private fun extractJsonObject(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || start >= end) return null
        return text.substring(start, end + 1)
    }

    private fun parseResult(rawText: String, root: JsonObject): GemmaAnalysisResult {
        val ocrText = root["ocrText"]?.jsonPrimitive?.contentOrNull
        val contextSummary = root["contextSummary"]?.jsonPrimitive?.contentOrNull
        val ingredients = root["ingredients"]?.jsonArray?.mapNotNull { element ->
            runCatching { parseIngredient(element.jsonObject) }.getOrNull()
        } ?: emptyList()
        return GemmaAnalysisResult(
            rawText = rawText,
            ocrText = ocrText,
            contextSummary = contextSummary,
            ingredients = ingredients,
        )
    }

    private fun parseIngredient(obj: JsonObject): GemmaAnalyzedIngredient {
        val confidence = obj["confidence"]?.jsonPrimitive?.floatOrNull?.coerceIn(0f, 1f)
        return GemmaAnalyzedIngredient(
            name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            quantityText = obj["quantityText"]?.jsonPrimitive?.contentOrNull,
            categoryText = obj["categoryText"]?.jsonPrimitive?.contentOrNull,
            storageHint = obj["storageHint"]?.jsonPrimitive?.contentOrNull,
            confidence = confidence,
        )
    }
}
