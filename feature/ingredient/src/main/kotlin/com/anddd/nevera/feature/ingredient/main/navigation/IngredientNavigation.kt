package com.anddd.nevera.feature.ingredient.main.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anddd.nevera.feature.ingredient.main.IngredientScreen
import com.anddd.nevera.feature.ingredient.ocrerror.OcrErrorScreen
import com.anddd.nevera.feature.ingredient.photodetail.PhotoDetailScreen
import com.anddd.nevera.feature.ingredient.registersuccess.RegisterSuccessScreen

const val INGREDIENT_ROUTE = "ingredient"
internal const val ARG_IMAGE_URI = "imageUri"

const val OCR_ERROR_ROUTE = "ocr_error"

const val REGISTER_SUCCESS_ROUTE = "register_success"
private const val ARG_TOTAL_COST = "totalCost"

const val PHOTO_DETAIL_ROUTE = "photo_detail"
private const val ARG_PHOTO_IMAGE_URI = "photoImageUri"

/**
 * 영수증 캡처 화면 → 식재료 등록 화면 이동
 *
 * @param imageUri 캡처된 영수증 이미지 URI (multipart API 호출에 사용)
 */
fun NavController.navigateToIngredient(
    imageUri: String,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
) {
    navigate("$INGREDIENT_ROUTE?$ARG_IMAGE_URI=${Uri.encode(imageUri)}", navOptions(builder))
}

/**
 * 사진 상세 화면으로 이동
 *
 * @param imageUri 표시할 이미지 URI (content:// 등 특수문자 포함 가능 — 내부에서 인코딩 처리)
 */
private fun NavController.navigateToPhotoDetail(imageUri: String) {
    navigate("$PHOTO_DETAIL_ROUTE?$ARG_PHOTO_IMAGE_URI=${Uri.encode(imageUri)}")
}

/**
 * 등록 성공 화면으로 이동
 *
 * @param totalCost 등록한 식재료의 총 금액
 * @param builder NavOptions DSL 빌더 (스택 조작 등)
 */
private fun NavController.navigateToRegisterSuccess(
    totalCost: Int,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
) {
    navigate("$REGISTER_SUCCESS_ROUTE/$totalCost", navOptions(builder))
}

fun NavGraphBuilder.ingredientNavGraph(
    navController: NavController,
    onNavigateToHome: () -> Unit,
) {
    ingredientScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        onNavigateToError = {
            navController.navigate(OCR_ERROR_ROUTE)
        },
        onNavigateToSuccess = { totalCost ->
            navController.navigateToRegisterSuccess(totalCost) {
                popUpTo(INGREDIENT_ROUTE) { inclusive = true }
            }
        },
        onNavigateToPhotoDetail = { imageUri ->
            navController.navigateToPhotoDetail(imageUri)
        },
    )
    ocrErrorScreen(
        onRetry = {
            val imageUri = navController.previousBackStackEntry
                ?.arguments
                ?.getString(ARG_IMAGE_URI)

            if (imageUri == null) {
                navController.popBackStack()
            } else {
                navController.navigateToIngredient(imageUri) {
                    popUpTo(INGREDIENT_ROUTE) { inclusive = true }
                }
            }
        },
        onClose = {
            navController.popBackStack()
        },
    )
    registerSuccessScreen(
        onClose = onNavigateToHome,
    )
    photoDetailScreen(
        onClose = {
            navController.popBackStack()
        },
    )
}

fun NavGraphBuilder.ingredientScreen(
    onNavigateBack: () -> Unit,
    onNavigateToError: () -> Unit,
    onNavigateToSuccess: (totalCost: Int) -> Unit,
    onNavigateToPhotoDetail: (imageUri: String) -> Unit,
) {
    composable(
        route = "$INGREDIENT_ROUTE?$ARG_IMAGE_URI={$ARG_IMAGE_URI}",
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) { type = NavType.StringType },
        ),
    ) {
        IngredientScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToError = onNavigateToError,
            onNavigateToSuccess = onNavigateToSuccess,
            onNavigateToPhotoDetail = onNavigateToPhotoDetail,
        )
    }
}

fun NavGraphBuilder.ocrErrorScreen(
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    composable(route = OCR_ERROR_ROUTE) {
        OcrErrorScreen(
            onRetry = onRetry,
            onClose = onClose,
        )
    }
}

fun NavGraphBuilder.registerSuccessScreen(
    onClose: () -> Unit,
) {
    composable(
        route = "$REGISTER_SUCCESS_ROUTE/{$ARG_TOTAL_COST}",
        arguments = listOf(
            navArgument(ARG_TOTAL_COST) { type = NavType.IntType },
        ),
    ) { backStackEntry ->
        val totalCost = backStackEntry.arguments?.getInt(ARG_TOTAL_COST) ?: 0
        RegisterSuccessScreen(
            totalSavedAmount = totalCost,
            onViewFridge = onClose,
            onClose = onClose,
        )
    }
}

fun NavGraphBuilder.photoDetailScreen(
    onClose: () -> Unit,
) {
    composable(
        route = "$PHOTO_DETAIL_ROUTE?$ARG_PHOTO_IMAGE_URI={$ARG_PHOTO_IMAGE_URI}",
        arguments = listOf(
            navArgument(ARG_PHOTO_IMAGE_URI) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) { backStackEntry ->
        val imageUri = backStackEntry.arguments?.getString(ARG_PHOTO_IMAGE_URI)
        PhotoDetailScreen(
            imageUri = imageUri,
            onClose = onClose,
        )
    }
}
