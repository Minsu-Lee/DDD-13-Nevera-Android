package com.anddd.nevera.infra.ai.performance

import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class GemmaImageAnalysisTrace(
    val traceId: String = UUID.randomUUID().toString(),
) {

    fun <T> measure(stage: String, block: () -> T): T {
        val startedAtNs = System.nanoTime()
        return try {
            block()
        } finally {
            record(stage, elapsedMs(startedAtNs))
        }
    }

    fun record(stage: String, elapsedMs: Long, detail: String? = null) {
        if (detail == null) {
            Timber.d("GemmaImageAnalysisPerf traceId=%s %s=%dms", traceId, stage, elapsedMs)
        } else {
            Timber.d("GemmaImageAnalysisPerf traceId=%s %s=%dms %s", traceId, stage, elapsedMs, detail)
        }
    }

    fun value(name: String, value: Any?) {
        Timber.d("GemmaImageAnalysisPerf traceId=%s %s=%s", traceId, name, value)
    }

    internal companion object {
        fun elapsedMs(startedAtNs: Long): Long =
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNs)
    }
}
