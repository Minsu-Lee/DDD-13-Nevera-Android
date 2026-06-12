package com.anddd.nevera.feature.fridge.edit.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.component.appbar.NeveraAppBar
import com.anddd.nevera.core.designsystem.component.appbar.NeveraAppBarNavigation
import com.anddd.nevera.core.designsystem.component.button.NeveraButtonSize
import com.anddd.nevera.core.designsystem.component.button.NeveraFilledButton
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.core.ui.displayName
import com.anddd.nevera.domain.model.ingredient.StorageLocation
import com.anddd.nevera.feature.fridge.R
import com.anddd.nevera.feature.fridge.edit.model.EditFridgeIngredientIntent
import com.anddd.nevera.feature.fridge.edit.model.EditFridgeIngredientUiState
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun EditFridgeIngredientContent(
    uiState: EditFridgeIngredientUiState,
    onIntent: (EditFridgeIngredientIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCategorySheet by remember { mutableStateOf(false) }
    var showStorageLocationSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            NeveraAppBar(
                title = stringResource(R.string.edit_fridge_ingredient_title),
                navigation = NeveraAppBarNavigation.Close(onClick = onNavigateBack),
            )
        },
        bottomBar = {
            NeveraFilledButton(
                label = stringResource(R.string.edit_fridge_ingredient_confirm),
                onClick = { onIntent(EditFridgeIngredientIntent.ConfirmClick) },
                size = NeveraButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.name,
                style = NeveraTheme.typography.headlineLarge,
                color = NeveraTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuantityRow(
                quantity = uiState.quantity,
                onDecrease = { onIntent(EditFridgeIngredientIntent.UpdateQuantity(uiState.quantity - 1)) },
                onIncrease = { onIntent(EditFridgeIngredientIntent.UpdateQuantity(uiState.quantity + 1)) },
            )

            EditFieldDivider()

            CostRow(
                cost = uiState.cost,
                onCostChange = { newValue ->
                    newValue.toIntOrNull()?.let { onIntent(EditFridgeIngredientIntent.UpdateCost(it)) }
                },
            )

            EditFieldDivider()

            SelectionRow(
                label = stringResource(R.string.edit_fridge_ingredient_category),
                value = uiState.category.displayName(),
                onClick = { showCategorySheet = true },
            )

            EditFieldDivider()

            SelectionRow(
                label = stringResource(R.string.edit_fridge_ingredient_storage_location),
                value = uiState.storageLocation.displayName(),
                onClick = { showStorageLocationSheet = true },
            )

            EditFieldDivider()

            SelectionRow(
                label = stringResource(R.string.edit_fridge_ingredient_expiry_date),
                value = uiState.expiryDate.format(dateFormatter),
                onClick = { showDatePicker = true },
            )
        }
    }

    if (showCategorySheet) {
        // TODO: CategoryBottomSheet 연동
    }

    if (showStorageLocationSheet) {
        // TODO: StorageLocationBottomSheet 연동
    }

    if (showDatePicker) {
        // TODO: DatePicker 연동
    }
}

@Composable
private fun StorageLocation.displayName(): String = when (this) {
    StorageLocation.Fridge -> stringResource(R.string.fridge_filter_refrigerator)
    StorageLocation.Freezer -> stringResource(R.string.fridge_filter_freezer)
    StorageLocation.Pantry -> stringResource(R.string.fridge_filter_room_temp)
}
