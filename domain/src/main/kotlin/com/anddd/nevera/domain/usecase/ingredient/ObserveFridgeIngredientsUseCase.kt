package com.anddd.nevera.domain.usecase.ingredient

import com.anddd.nevera.domain.model.ingredient.FridgeIngredient
import com.anddd.nevera.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFridgeIngredientsUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository,
) {

    operator fun invoke(): Flow<List<FridgeIngredient>> = ingredientRepository.observeFridgeIngredients()
}