package com.anddd.nevera.data.mapper

import com.anddd.nevera.core.common.NetworkError
import com.anddd.nevera.data.mapper.error.toCommonError
import com.anddd.nevera.data.model.fridge.ProcessIngredientResponse
import com.anddd.nevera.domain.model.ingredient.IngredientProcessResult
import com.anddd.nevera.domain.model.ingredient.ProcessIngredientError
import com.anddd.nevera.domain.model.ingredient.ProcessType
import timber.log.Timber

// ── Request 변환 ─────────────────────────────────────────────────────────────

internal fun ProcessType.toApiString(): String = when (this) {
    ProcessType.Consumed -> "CONSUMED"
    ProcessType.Wasted -> "WASTED"
}

// ── Response 변환 ─────────────────────────────────────────────────────────────

internal fun ProcessIngredientResponse.toDomain(): IngredientProcessResult =
    IngredientProcessResult(
        inventoryId = inventoryId,
        processedStatus = processedStatus.toProcessType(),
        processedRatio = processedRatio,
        processedAmount = processedAmount,
        consumedRatio = consumedRatio,
        wastedRatio = wastedRatio,
        remainingRatio = remainingRatio,
        remainingAmount = remainingAmount,
        inventoryStatus = inventoryStatus,
        completed = completed,
    )

private fun String.toProcessType(): ProcessType = when (this) {
    "CONSUMED" -> ProcessType.Consumed
    "WASTED" -> ProcessType.Wasted
    else -> ProcessType.Consumed
}

// ── Error 변환 ────────────────────────────────────────────────────────────────

private object ProcessIngredientErrorCode {
    const val INVENTORY_NOT_FOUND = 4001
    const val INVENTORY_FORBIDDEN = 4002
    const val ALREADY_COMPLETED = 4003
    const val INVALID_PROCESS_STATUS = 4004
    const val INVALID_PROCESS_RATIO = 4005
    const val PROCESS_RATIO_EXCEEDED = 4006
}

internal fun NetworkError.toProcessIngredientError(): ProcessIngredientError = when (this) {
    is NetworkError.HttpError -> when (code) {
        ProcessIngredientErrorCode.INVENTORY_NOT_FOUND -> ProcessIngredientError.InventoryNotFound
        ProcessIngredientErrorCode.INVENTORY_FORBIDDEN -> ProcessIngredientError.InventoryForbidden
        ProcessIngredientErrorCode.ALREADY_COMPLETED -> ProcessIngredientError.AlreadyCompleted
        ProcessIngredientErrorCode.INVALID_PROCESS_STATUS -> ProcessIngredientError.InvalidProcessStatus
        ProcessIngredientErrorCode.INVALID_PROCESS_RATIO -> ProcessIngredientError.InvalidProcessRatio
        ProcessIngredientErrorCode.PROCESS_RATIO_EXCEEDED -> ProcessIngredientError.ProcessRatioExceeded
        else -> ProcessIngredientError.Common(toCommonError())
    }

    else -> ProcessIngredientError.Common(toCommonError())
}.also {
    Timber.d("ProcessIngredientError[code=$code]: $message")
}
