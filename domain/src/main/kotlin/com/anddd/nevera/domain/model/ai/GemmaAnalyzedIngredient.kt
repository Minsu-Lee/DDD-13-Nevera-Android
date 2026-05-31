package com.anddd.nevera.domain.model.ai

data class GemmaAnalyzedIngredient(
    val name: String,
    val quantityText: String?,
    val categoryText: String?,
    val storageHint: String?,
    val confidence: Float?,
)
