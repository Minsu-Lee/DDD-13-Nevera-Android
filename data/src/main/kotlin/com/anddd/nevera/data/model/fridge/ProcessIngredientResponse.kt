package com.anddd.nevera.data.model.fridge

import com.google.gson.annotations.SerializedName

internal data class ProcessIngredientResponse(
    @SerializedName("inventoryId") val inventoryId: Long,
    /** 이번 처리 유형 ("CONSUMED" | "WASTED") */
    @SerializedName("processedStatus") val processedStatus: String,
    /** 이번 처리 비율 (%) */
    @SerializedName("processedRatio") val processedRatio: Int,
    /** 이번에 구조·폐기된 금액 */
    @SerializedName("processedAmount") val processedAmount: Int,
    /** 누적 구조 비율 (%) */
    @SerializedName("consumedRatio") val consumedRatio: Int,
    /** 누적 폐기 비율 (%) */
    @SerializedName("wastedRatio") val wastedRatio: Int,
    /** 남은 비율 (%) */
    @SerializedName("remainingRatio") val remainingRatio: Int,
    /** 현재 남은 금액 */
    @SerializedName("remainingAmount") val remainingAmount: Int,
    /** 부분 처리 시 "ACTIVE", 완료 시 최종 상태 */
    @SerializedName("inventoryStatus") val inventoryStatus: String,
    /** 누적 처리율 100% 여부 */
    @SerializedName("completed") val completed: Boolean,
)
