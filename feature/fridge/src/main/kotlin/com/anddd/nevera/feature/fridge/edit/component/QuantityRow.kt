package com.anddd.nevera.feature.fridge.edit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.icon.NeveraIcons
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.feature.fridge.R

@Composable
internal fun QuantityRow(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.edit_fridge_ingredient_quantity),
            style = NeveraTheme.typography.bodyMedium,
            color = NeveraTheme.colors.textSecondary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "-",
                style = NeveraTheme.typography.titleLarge,
                color = if (quantity > 1) NeveraTheme.colors.textPrimary else NeveraTheme.colors.textDisabled,
                modifier = Modifier.clickable(enabled = quantity > 1, onClick = onDecrease),
            )
            Text(
                text = quantity.toString(),
                style = NeveraTheme.typography.bodyLarge,
                color = NeveraTheme.colors.textPrimary,
            )
            Icon(
                painter = NeveraIcons.Plus,
                contentDescription = null,
                modifier = Modifier.clickable(onClick = onIncrease),
                tint = NeveraTheme.colors.textPrimary,
            )
        }
    }
}
