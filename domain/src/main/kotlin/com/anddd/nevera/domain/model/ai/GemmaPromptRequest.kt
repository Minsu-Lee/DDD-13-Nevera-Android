package com.anddd.nevera.domain.model.ai

data class GemmaPromptRequest(
    val prompt: String,
    val systemPrompt: String? = null,
    val maxTokens: Int = 512,
)
