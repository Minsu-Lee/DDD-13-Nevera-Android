# Gemma4 E2B AI Pack 사용 가이드

## 개요

- 모델 파일: `gemma4-e2b-it.litertlm` (~2.59 GB, 3개 AI Pack으로 분할)
- 전달 방식: Play AI Delivery (on-demand)
- 상태 API: `Flow<GemmaModelState>` (ObserveGemmaModelStateUseCase)

## 1. 다운로드 시작

UI/기획이 원하는 시점에 호출한다. 앱 시작 시 자동 호출하지 않는다.

```kotlin
viewModelScope.launch {
    requestGemmaModelDownloadUseCase()
}
```

## 2. 상태 구독 (MVI 컨벤션 예시)

향후 feature 모듈에 ViewModel을 만들 때는 프로젝트 MVI 패턴을 따른다.

```kotlin
@HiltViewModel
class GemmaDownloadViewModel @Inject constructor(
    private val observeGemmaModelState: ObserveGemmaModelStateUseCase,
    private val requestGemmaModelDownload: RequestGemmaModelDownloadUseCase,
    private val cancelGemmaModelDownload: CancelGemmaModelDownloadUseCase,
) : NeveraViewModel<
    GemmaDownloadUiState,
    GemmaDownloadSideEffect,
    GemmaDownloadIntent,
    GemmaDownloadMutation
>(GemmaDownloadUiState()) {

    init {
        intent {
            observeGemmaModelState().collect { state ->
                applyMutation(GemmaDownloadMutation.StateChanged(state))
            }
        }
    }

    override fun handleIntent(intent: GemmaDownloadIntent) = when (intent) {
        GemmaDownloadIntent.StartDownload -> intent { requestGemmaModelDownload() }
        GemmaDownloadIntent.CancelDownload -> intent { cancelGemmaModelDownload() }
    }

    override suspend fun Syntax<GemmaDownloadUiState, GemmaDownloadSideEffect>.applyMutation(
        mutation: GemmaDownloadMutation,
    ) {
        when (mutation) {
            is GemmaDownloadMutation.StateChanged ->
                reduce { state.copy(modelState = mutation.modelState) }
        }
    }
}
```

UiState에서 진행률 표시:

```kotlin
data class GemmaDownloadUiState(
    val modelState: GemmaModelState = GemmaModelState.NotRequested,
) : NeveraState {
    val downloadPercent: Float?
        get() = when (modelState) {
            is GemmaModelState.Downloading -> modelState.percent
            else -> null
        }
    val isDownloading: Boolean
        get() = modelState is GemmaModelState.Downloading || modelState is GemmaModelState.Pending
}
```

Compose Screen에서:

```kotlin
viewModel.collectAsState().value.let { uiState ->
    when (val state = uiState.modelState) {
        is GemmaModelState.Downloading -> {
            LinearProgressIndicator(progress = state.percent)
        }
        GemmaModelState.WaitingForWifi -> {
            // WaitingForWifi 안내 UI
        }
        GemmaModelState.RequiresUserConfirmation -> {
            // 아래 "사용자 확인 다이얼로그" 참고
        }
        is GemmaModelState.Ready -> {
            // state.modelPath로 모델 사용
        }
        else -> Unit
    }
}
```

## 3. 취소

```kotlin
viewModelScope.launch {
    cancelGemmaModelDownloadUseCase()
}
```

- `cancelGemmaModelDownload()`는 즉시 `Canceling`을 emit하고 Play API의 best-effort cancel을 호출한다.
- cancel 후 Play API가 `CANCELED` 상태를 반환하면 `Canceled`를 emit한다.
- 이미 `Ready` 상태인 모델은 cancel 대상이 아니다. 삭제는 별도 요구사항이다.
- cancel 후 `requestGemmaModelDownload()`를 다시 호출하면 처음부터 재요청된다.

## 4. WaitingForWifi / RequiresUserConfirmation 처리

Play AI Delivery가 Wi-Fi 또는 사용자 확인을 요구하면 이 상태가 발생한다.

UI layer에서 `GemmaDownloadConfirmationLauncher`를 사용해 시스템 다이얼로그를 띄운다.

```kotlin
// Activity/Fragment에서 ActivityResultLauncher 등록
private val confirmationLauncher = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // 사용자 확인 완료 — Play가 자동으로 다운로드 재개
    }
}

// RequiresUserConfirmation 상태에서 다이얼로그 표시
fun onRequiresUserConfirmation(state: AiPackState) {
    confirmationLauncher.showConfirmationDialog(
        activity = this,
        state = state,
        launcher = confirmationLauncher,
    )
}
```

## 5. 모델 경로 조회

```kotlin
val modelPath = getGemmaModelPathUseCase() // 준비 안 됐으면 null
```

## 6. 모델 파일 준비 (개발 환경)

실기기에서 Gemma 추론을 테스트하려면 모델 파일을 직접 준비해야 한다.
모델 파일은 git에 포함되지 않으므로 팀원 각자가 아래 절차를 수행한다.

### 6-1. 모델 파일 다운로드

HuggingFace에서 `.litertlm` 파일을 받는다.

1. `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm` 접속
2. HuggingFace 계정 로그인 후 **Google Gemma Terms of Use** 동의
   - 동의가 활성화되려면 HuggingFace 프로필 → Settings → Connected Apps에서 Google 계정 연결 필요
3. **Files and versions** 탭에서 `gemma4-e2b-it.litertlm` (~2.59 GB) 다운로드
4. 다운로드한 파일을 원하는 위치에 저장 (프로젝트 외부 권장, 예: `~/Downloads/`)

### 6-2. 준비 스크립트 실행

모델 파일 경로를 인자로 넘겨 스크립트를 실행한다.
파일은 어디에 있어도 무관하며, 스크립트가 3개 AI Pack 디렉터리에 자동으로 분할·배치한다.

```bash
# 프로젝트 루트에서 실행
bash infra/ai/scripts/prepare_gemma4_e2b_model.sh ~/Downloads/gemma4-e2b-it.litertlm
```

스크립트 완료 후 생성되는 파일 (모두 .gitignore에 포함되어 git 추적 안 됨):

```
gemma4_e2b_pack_01/src/main/assets/gemma4/gemma4-e2b-it.litertlm.part01
gemma4_e2b_pack_02/src/main/assets/gemma4/gemma4-e2b-it.litertlm.part02
gemma4_e2b_pack_03/src/main/assets/gemma4/gemma4-e2b-it.litertlm.part03
infra/ai/model-artifacts/gemma4_e2b_manifest.json  ← SHA-256 포함
```

### 6-3. (선택) 체크섬 검증 활성화

`infra/ai/model-artifacts/gemma4_e2b_manifest.json`의 SHA-256 값을
`GemmaAiPackConstants`의 `EXPECTED_FULL_SHA256`, `EXPECTED_PART_SHA256`에 채워 넣으면
merge 후 checksum 검증이 활성화된다. 1차 개발에서는 null(비활성)이 기본값이다.

## 7. Play 내부 테스트에서 실제 다운로드 검증

AI Pack 다운로드/취소는 실제 Play Store 환경에서만 동작한다.

- **내부 테스트 트랙**: Google Play Console → 내부 테스트 → AAB 업로드 후 디바이스에서 설치
- `./gradlew bundleDebug`로 AAB를 빌드하면 AI Pack이 포함된다
- 로컬 에뮬레이터/USB 설치(`adb install`)에서는 AI Pack이 동작하지 않는다

## 8. 상태 흐름 요약

```
NotRequested
  └─ requestGemmaModelDownload() ──→ Checking
                                      ├─ (model ready) ──→ Ready
                                      ├─ (fetch) ──→ Pending ──→ Downloading ──→ Transferring ──→ (all COMPLETED) ──→ Ready
                                      │                       └─ WaitingForWifi / RequiresUserConfirmation
                                      └─ (error) ──→ Failed

  cancelGemmaModelDownload() ──→ Canceling ──→ Canceled
                                               └─ requestGemmaModelDownload() 재호출 가능
```
