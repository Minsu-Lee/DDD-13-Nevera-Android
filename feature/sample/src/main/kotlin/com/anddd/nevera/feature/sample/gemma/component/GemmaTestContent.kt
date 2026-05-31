package com.anddd.nevera.feature.sample.gemma.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.feature.sample.R
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestIntent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestUiState

@Composable
fun GemmaTestContent(
    uiState: GemmaTestUiState,
    onIntent: (GemmaTestIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("모델 상태: ${uiState.modelState::class.simpleName}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onIntent(GemmaTestIntent.DownloadModel) },
                enabled = uiState.modelState !is GemmaModelState.Downloading &&
                    uiState.modelState !is GemmaModelState.Pending,
            ) {
                Text("다운로드")
            }
            Button(
                onClick = { onIntent(GemmaTestIntent.CancelDownload) },
                enabled = uiState.modelState is GemmaModelState.Downloading ||
                    uiState.modelState is GemmaModelState.Pending,
            ) {
                Text("취소")
            }
        }

        HorizontalDivider()

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = { onIntent(GemmaTestIntent.UpdatePrompt(it)) },
            label = { Text("프롬프트") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        OutlinedTextField(
            value = uiState.imageUri,
            onValueChange = { onIntent(GemmaTestIntent.UpdateImageUri(it)) },
            label = { Text("이미지 URI (content:// 또는 file://)") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedButton(
            onClick = { onIntent(GemmaTestIntent.OpenImagePicker) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.gemma_test_image_picker_button))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onIntent(GemmaTestIntent.RunPrompt) },
                enabled = !uiState.isGenerating && uiState.modelState is GemmaModelState.Ready,
            ) {
                Text("프롬프트 실행")
            }
            Button(
                onClick = { onIntent(GemmaTestIntent.RunImageAnalysis) },
                enabled = !uiState.isGenerating &&
                    uiState.modelState is GemmaModelState.Ready &&
                    uiState.imageUri.isNotBlank(),
            ) {
                Text("이미지 분석")
            }
            Button(
                onClick = { onIntent(GemmaTestIntent.ClearResult) },
                enabled = !uiState.isGenerating,
            ) {
                Text("초기화")
            }
        }

        if (uiState.isGenerating) CircularProgressIndicator()

        if (uiState.errorMessage != null) {
            Text("오류: ${uiState.errorMessage}")
        }

        if (uiState.streamingText.isNotEmpty()) {
            HorizontalDivider()
            Text("생성 결과:")
            Text(uiState.streamingText)
        }

        if (uiState.analysisResult != null) {
            HorizontalDivider()
            Text("OCR: ${uiState.analysisResult.ocrText}")
            Text("요약: ${uiState.analysisResult.contextSummary}")
            Text("식재료 (${uiState.analysisResult.ingredients.size}개):")
            uiState.analysisResult.ingredients.forEach { ing ->
                Text("• ${ing.name} / ${ing.quantityText} / ${ing.categoryText} / ${ing.storageHint} (신뢰도: ${ing.confidence})")
            }
        }
    }
}
