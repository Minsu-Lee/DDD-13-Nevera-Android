package com.anddd.nevera.feature.fridge.main.model

import com.anddd.nevera.core.mvi.NeveraIntent

sealed interface FridgeIntent : NeveraIntent {
    data object Load : FridgeIntent
    data object Submit : FridgeIntent
}
