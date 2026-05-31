package com.anddd.nevera.domain.model.ai

data class GemmaAnalysisResult(
    val rawText: String,
    val ocrText: String?,
    val contextSummary: String?,
    val ingredients: List<GemmaAnalyzedIngredient>,
)
