package com.anddd.nevera.feature.fridge.main.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anddd.nevera.core.designsystem.component.appbar.NeveraAppBarAction
import com.anddd.nevera.core.designsystem.component.appbar.NeveraDisplayAppBar
import com.anddd.nevera.core.designsystem.component.button.NeveraButtonSize
import com.anddd.nevera.core.designsystem.component.button.NeveraFilledButton
import com.anddd.nevera.core.designsystem.icon.NeveraIcons
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.core.ui.component.LoadingContent
import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.domain.model.ingredient.StorageLocation
import com.anddd.nevera.feature.fridge.R
import com.anddd.nevera.feature.fridge.main.model.FridgeIngredientUiModel
import com.anddd.nevera.feature.fridge.main.model.FridgeIntent
import com.anddd.nevera.feature.fridge.main.model.FridgeUiState
import com.anddd.nevera.feature.fridge.main.model.StorageLocationFilter
import java.time.LocalDate

@Composable
internal fun FridgeContent(
    uiState: FridgeUiState,
    onIntent: (FridgeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NeveraDisplayAppBar(
                title = stringResource(R.string.fridge_title),
                action = NeveraAppBarAction.Icons.of(
                    NeveraAppBarAction.Icons.Item(
                        painter = if (uiState.hasUnreadNotification) NeveraIcons.BellOn else NeveraIcons.Bell,
                        contentDescription = stringResource(R.string.fridge_notification_icon_desc),
                        onClick = { onIntent(FridgeIntent.NotificationIconClicked) },
                    ),
                ),
            )
        },
        floatingActionButton = {
            NeveraFilledButton(
                label = stringResource(R.string.fridge_fab_label),
                onClick = { onIntent(FridgeIntent.AddIngredientClick) },
                size = NeveraButtonSize.Medium,
                shape = RoundedCornerShape(NeveraTheme.radius.max),
            )
        },
        containerColor = NeveraTheme.colors.surfacePrimary,
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Spacer(modifier = Modifier.height(NeveraTheme.spacing.gap16))
                }
                item {
                    FridgeFilterTabRow(
                        selectedFilter = uiState.selectedStorageFilter,
                        onFilterSelected = { onIntent(FridgeIntent.SelectStorageFilter(it)) },
                        modifier = Modifier.padding(horizontal = NeveraTheme.spacing.padding16),
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(NeveraTheme.spacing.gap16))
                }
                item {
                    FridgeCategoryFilterRow(
                        selectedFilter = uiState.selectedCategoryFilter,
                        onFilterSelected = { onIntent(FridgeIntent.SelectCategoryFilter(it)) },
                    )
                }
                item {
                    FridgeIngredientListHeader(
                        totalCount = uiState.totalCount,
                        selectedStorageFilter = uiState.selectedStorageFilter,
                        selectedSortOrder = uiState.selectedSortOrder,
                        onSortOrderSelected = { onIntent(FridgeIntent.SelectSortOrder(it)) },
                        modifier = Modifier.padding(top = NeveraTheme.spacing.gap8),
                    )
                }
                // TODO: 페이지네이션 아이템 목록
                items(mockIngredients, key = { it.id }) { item ->
                    FridgeIngredientItem(
                        item = item,
                        onRescueClick = {},
                        onDisposeClick = {},
                        onMoreClick = {},
                    )
                }
            }
            if (uiState.isLoading) {
                LoadingContent()
            }
        }
    }
}

private val mockIngredients = listOf(
    FridgeIngredientUiModel(1L, "제주 햇당근", FoodCategory.Veg, "채소", 1, 6500, LocalDate.now().plusDays(28)),
    FridgeIngredientUiModel(2L, "삼겹살", FoodCategory.MeatEggs, "육류/계란", 1, 0, LocalDate.now().minusDays(3)),
    FridgeIngredientUiModel(3L, "서울우유 1L", FoodCategory.Dairy, "유제품", 2, 3200, LocalDate.now().plusDays(5)),
    FridgeIngredientUiModel(4L, "새우", FoodCategory.Sea, "수산물", 3, 12000, LocalDate.now().plusDays(1)),
    FridgeIngredientUiModel(5L, "청포도", FoodCategory.Fruit, "과일", 1, 4500, LocalDate.now().plusDays(14)),
    FridgeIngredientUiModel(6L, "간장", FoodCategory.Sauce, "소스/양념", 1, 2800, LocalDate.now().plusDays(180)),
    FridgeIngredientUiModel(7L, "콜라 1.5L", FoodCategory.Drink, "음료", 2, 1800, LocalDate.now().plusDays(90)),
    FridgeIngredientUiModel(8L, "두부", FoodCategory.Processed, "가공식품", 1, 1500, LocalDate.now()),
    FridgeIngredientUiModel(9L, "계란 10구", FoodCategory.MeatEggs, "육류/계란", 10, 3000, LocalDate.now().minusDays(1)),
    FridgeIngredientUiModel(10L, "오징어", FoodCategory.Sea, "수산물", 2, 8000, LocalDate.now().plusDays(3)),
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FridgeContentPreview() {
    NeveraTheme {
        FridgeContent(
            uiState = FridgeUiState(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FridgeContentFridgeSelectedPreview() {
    NeveraTheme {
        FridgeContent(
            uiState = FridgeUiState(
                selectedStorageFilter = StorageLocationFilter.Specific(StorageLocation.Fridge),
            ),
            onIntent = {},
        )
    }
}
