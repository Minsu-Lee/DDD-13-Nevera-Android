package com.anddd.nevera.feature.main.home

import androidx.annotation.DrawableRes
import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.feature.main.R

@DrawableRes
internal fun FoodCategory.iconRes(): Int = when (this) {
    FoodCategory.Vegetable -> R.drawable.img_listcell_vegetable
    FoodCategory.Fruit -> R.drawable.img_listcell_fruit
    FoodCategory.MeatEgg -> R.drawable.img_listcell_meat
    FoodCategory.Seafood -> R.drawable.img_listcell_seafood
    FoodCategory.Dairy -> R.drawable.img_listcell_dairy
    FoodCategory.Sauce -> R.drawable.img_listcell_sauce
    FoodCategory.Beverage -> R.drawable.img_listcell_beverage
    FoodCategory.Processed -> R.drawable.img_listcell_processed
    FoodCategory.Other -> R.drawable.img_listcell_etc
}
