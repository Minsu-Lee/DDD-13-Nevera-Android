package com.anddd.nevera.feature.main.home

import com.anddd.nevera.core.common.onFailure
import com.anddd.nevera.core.common.onSuccess
import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.domain.usecase.home.GetHomeSummaryUseCase
import com.anddd.nevera.domain.usecase.ingredient.GetRescuedIngredientsUseCase
import com.anddd.nevera.feature.main.home.model.HomeIntent
import com.anddd.nevera.feature.main.home.model.HomeMutation
import com.anddd.nevera.feature.main.home.model.HomeProfileUiModel
import com.anddd.nevera.feature.main.home.model.HomeSavingsUiModel
import com.anddd.nevera.feature.main.home.model.HomeSideEffect
import com.anddd.nevera.feature.main.home.model.HomeUiState
import com.anddd.nevera.feature.main.home.model.HomeWishUiModel
import com.anddd.nevera.feature.main.home.model.IngredientFilterTab
import com.anddd.nevera.feature.main.home.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeSummary: GetHomeSummaryUseCase,
    private val getRescuedIngredients: GetRescuedIngredientsUseCase,
) : NeveraViewModel<HomeUiState, HomeSideEffect, HomeIntent, HomeMutation>(HomeUiState()) {

    init {
        load()
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.RecentIngredientTabClick -> onRecentIngredientTabClick(intent.tab)
            HomeIntent.AddIngredientClick -> Unit // TODO: 식재료 추가 화면 이동
        }
    }

    private fun load() = intent {
        applyMutation(HomeMutation.Loading)

        val (summaryResult, ingredientsResult) = coroutineScope {
            val summaryDeferred = async { getHomeSummary() }
            val ingredientsDeferred = async { getRescuedIngredients() }
            summaryDeferred.await() to ingredientsDeferred.await()
        }

        summaryResult
            .onSuccess { summary ->
                applyMutation(HomeMutation.ShowProfile(HomeProfileUiModel(summary.nickname)))
                summary.wish?.let { wish ->
                    applyMutation(
                        HomeMutation.ShowWish(
                            HomeWishUiModel(
                                name = wish.name,
                                goalAmount = wish.goalAmount,
                                accumulatedAmount = wish.accumulatedAmount,
                                remainingAmount = wish.remainingAmount,
                                isAchieved = wish.isAchieved
                            )
                        )
                    )
                } ?: applyMutation(HomeMutation.ShowEmptyWish)
                applyMutation(
                    HomeMutation.ShowSavings(
                        HomeSavingsUiModel(
                            rescuedAmount = summary.rescuedAmount,
                            dispositionAmount = summary.dispositionAmount,
                        )
                    )
                )
            }
            .onFailure {
                // TODO 네트워크 에러 처리
            }

        ingredientsResult
            .onSuccess { ingredients ->
                applyMutation(HomeMutation.ShowRescuedIngredients(ingredients.toUiModel()))
            }
            .onFailure {
                // TODO 네트워크 에러 처리
            }

        applyMutation(HomeMutation.LoadComplete)
    }

    private fun onRecentIngredientTabClick(tab: IngredientFilterTab) = intent {
        applyMutation(HomeMutation.SetRecentIngredientFilterTab(tab))
    }

    override suspend fun Syntax<HomeUiState, HomeSideEffect>.applyMutation(mutation: HomeMutation) {
        when (mutation) {
            HomeMutation.Loading -> reduce { state.copy(isLoading = true) }

            HomeMutation.LoadComplete -> reduce { state.copy(isLoading = false) }

            is HomeMutation.SetRecentIngredientFilterTab -> reduce {
                state.copy(ingredientFilterTab = mutation.tab)
            }

            is HomeMutation.ShowProfile -> reduce { state.copy(profile = mutation.profile) }
            is HomeMutation.ShowWish -> reduce { state.copy(wish = mutation.wish) }
            HomeMutation.ShowEmptyWish -> reduce { state.copy(wish = null) }
            is HomeMutation.ShowSavings -> reduce { state.copy(savings = mutation.savings) }
            is HomeMutation.ShowRescuedIngredients -> reduce {
                state.copy(rescuedIngredients = mutation.ingredients)
            }
        }
    }
}
