package com.anddd.nevera.data.model.fridge

import com.google.gson.annotations.SerializedName

internal data class ProcessIngredientRequest(
    @SerializedName("status") val status: String,
    @SerializedName("ratio") val ratio: Int,
)
