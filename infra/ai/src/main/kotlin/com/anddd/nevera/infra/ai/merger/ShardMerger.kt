package com.anddd.nevera.infra.ai.merger

import com.anddd.nevera.domain.model.ai.GemmaModelState
import java.io.File

internal interface ShardMerger {
    fun isModelReady(): Boolean
    fun modelPath(): String
    fun merge(shardFiles: List<File>): GemmaModelState
}
