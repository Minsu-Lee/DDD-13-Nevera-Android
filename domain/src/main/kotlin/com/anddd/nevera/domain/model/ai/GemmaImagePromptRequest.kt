package com.anddd.nevera.domain.model.ai

data class GemmaImagePromptRequest(
    val imageUri: String,
    val prompt: String,
    val systemPrompt: String? = null,
    val maxTokens: Int = 768,
)
