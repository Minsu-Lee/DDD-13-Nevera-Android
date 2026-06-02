# Gemma4 프롬프트 & 이미지 OCR/Context 분석 사용 가이드

## 개요

LiteRT-LM Android API(`com.google.ai.edge.litertlm:litertlm-android:0.12.0`)를 사용해
온디바이스에서 텍스트 프롬프트와 이미지+프롬프트 요청을 처리한다.

모델 다운로드/준비는 1차 개발에서 구축한 AI Pack 인프라(`GemmaModelState`, `GetGemmaModelPathUseCase`)를 통해 처리하고, 이 레이어에서는 모델 경로만 받아 엔진을 초기화한다.

---

## 핵심 정책

- **모델 미준비 시 자동 다운로드 없음** — `GetGemmaModelPathUseCase()`가 null이면 `Failed(ModelNotReady)`를 반환하고 종료
- **기존 서버 OCR/SSE 흐름 영향 없음** — `ExtractIngredientsUseCase` 등은 별도 동작
- **Streaming Flow API** — `Flow<GemmaGenerationEvent>` (`Started → Token × n → Completed or Failed`)

---

## UseCase 목록

| UseCase | 설명 |
|---|---|
| `GenerateGemmaPromptUseCase(request)` | 텍스트 프롬프트 streaming 생성 |
| `AnalyzeGemmaImageWithPromptUseCase(request)` | 이미지+프롬프트 streaming 분석 |
| `ParseGemmaAnalysisResultUseCase(rawText)` | raw 응답 텍스트 → `GemmaAnalysisResult` |

---

## ViewModel 연결 예시 (MVI 컨벤션)

```kotlin
@HiltViewModel
class GemmaViewModel @Inject constructor(
    private val observeGemmaModelState: ObserveGemmaModelStateUseCase,
    private val generateGemmaPrompt: GenerateGemmaPromptUseCase,
    private val parseGemmaAnalysisResult: ParseGemmaAnalysisResultUseCase,
) : NeveraViewModel<GemmaUiState, GemmaSideEffect, GemmaIntent, GemmaMutation>(GemmaUiState()) {

    init {
        intent {
            observeGemmaModelState().collect { modelState ->
                applyMutation(GemmaMutation.ModelStateChanged(modelState))
            }
        }
    }

    override fun handleIntent(action: GemmaIntent) = when (action) {
        GemmaIntent.RunPrompt -> runPrompt()
    }

    private fun runPrompt() = intent {
        generateGemmaPrompt(GemmaPromptRequest(prompt = state.prompt)).collect { event ->
            when (event) {
                GemmaGenerationEvent.Started -> applyMutation(GemmaMutation.Started)
                is GemmaGenerationEvent.Token -> applyMutation(GemmaMutation.Token(event.text))
                is GemmaGenerationEvent.Completed -> {
                    applyMutation(GemmaMutation.Completed(event.fullText))
                    // 구조화 결과가 필요하면 Completed 이후 ParseGemmaAnalysisResultUseCase 호출
                }
                is GemmaGenerationEvent.Failed -> applyMutation(GemmaMutation.Failed(event.error))
            }
        }
    }
}
```

---

## 이미지 multimodal API

LiteRT-LM 0.12.0 기준:
- `Content.ImageFile(absolutePath)` — 로컬 JPEG 파일 경로를 전달
- `Content.Text(text)` — 프롬프트 텍스트
- `Contents.of(Content.ImageFile(...), Content.Text(...))` — 복합 콘텐츠 구성

이미지는 `GemmaImageNormalizer`를 통해 `context.cacheDir/gemma4/images/`에 임시 JPEG로 변환 후 전달한다 (긴 변 1600px 이하, quality 88, 최대 1.5MB).

---

## JSON 파싱

`ParseGemmaAnalysisResultUseCase`는 raw 텍스트에서 JSON을 추출해 `GemmaAnalysisResult`로 변환한다.

```kotlin
val parseResult = parseGemmaAnalysisResult(fullText)
parseResult.onSuccess { result ->
    // result.ocrText, result.contextSummary, result.ingredients
}.onFailure { error ->
    // GemmaGenerationError.ResponseParseFailed
}
```

파싱 실패 시에도 `Completed(fullText)` 이벤트는 그대로 emit되므로 원본 텍스트를 항상 UI/로그에서 보존할 수 있다.

---

## 개발/검증 화면

`feature:sample` 모듈의 `GemmaTestScreen`에서 수동 검증 가능:

1. `NavHost`에서 `navController.navigate(GemmaTestRoute)` 호출
2. 로컬 USB 설치 앱에서는 모델 다운로드/취소를 테스트하지 않는다.
3. 먼저 앱 내부 저장소에 단일 모델 파일을 주입한다.

```bash
# 원본 .litertlm 파일이 있는 경우
bash infra/ai/scripts/install_gemma4_e2b_model_to_device.sh ~/Downloads/gemma4-e2b-it.litertlm

# 원본 파일 없이 AI Pack shard만 있는 경우
bash infra/ai/scripts/install_gemma4_e2b_model_to_device.sh --from-shards
```

4. `GemmaTestScreen`에서 텍스트/이미지 prompt를 실행한다.

로컬 샘플이 기대하는 모델 위치:

```
/data/user/0/com.anddd.nevera/no_backup/gemma4/gemma4-e2b-it.litertlm
```

이 경로는 프로젝트 폴더가 아니라 설치된 앱의 내부 저장소(`Context.noBackupFilesDir`)이다.
해당 파일이 없거나 SHA-256 검증에 실패하면 `GetGemmaModelPathUseCase()`가 null을 반환하고,
엔진은 `Failed(ModelNotReady)`를 emit한다.

Play AI Delivery 다운로드/취소 검증은 Google Play 내부 테스트 트랙에서 설치한 앱으로만 수행한다.

---

## 주의사항

- `Engine.initialize()`는 수 초 이상 소요될 수 있으므로 반드시 IO thread에서 실행 (`Dispatchers.IO` 사용)
- 동일 `modelPath`에 대해 엔진을 재사용하며, `modelPath`가 변경되면 기존 엔진을 close 후 재초기화
- `close()` API 제공 — 앱이 Background로 가는 상황에서의 자동 close는 호출 측에서 판단
