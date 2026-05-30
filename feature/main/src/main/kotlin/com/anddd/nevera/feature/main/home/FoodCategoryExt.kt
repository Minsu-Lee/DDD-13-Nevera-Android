package com.anddd.nevera.feature.main.home

import androidx.annotation.DrawableRes
import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.feature.main.R

@DrawableRes
internal fun FoodCategory.iconRes(): Int = when (this) {
    FoodCategory.Veg -> R.drawable.img_listcell_vegetable
    FoodCategory.Fruit -> R.drawable.img_listcell_fruit
    FoodCategory.MeatEggs -> R.drawable.img_listcell_meat
    FoodCategory.Sea -> R.drawable.img_listcell_seafood
    FoodCategory.Dairy -> R.drawable.img_listcell_dairy
    FoodCategory.Sauce -> R.drawable.img_listcell_sauce
    FoodCategory.Drink -> R.drawable.img_listcell_beverage
    FoodCategory.Processed -> R.drawable.img_listcell_processed
    FoodCategory.Etc -> R.drawable.img_listcell_etc
}
