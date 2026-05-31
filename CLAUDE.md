# Nevera Android 개발 가이드

## feature 모듈 — Presentation Layer 규칙

feature 모듈의 Presentation Layer는 MVI를 표준 패턴으로 사용한다. 각 타입의 개념적 정의와 목적은 다음과 같다.

| 타입 | 개념적 정의 | 목적 |
|---|---|---|
| `*Intent` | 사용자가 화면에서 수행한 액션 | "무엇을 했는가"를 타입으로 명시. `sealed interface`로 선언해 화면의 가능한 액션 목록을 한눈에 파악할 수 있게 한다. |
| `*UiState` | 화면이 렌더링에 사용하는 단일 상태 | "현재 화면이 어떻게 보여야 하는가"를 표현. ViewModel 생존 범위 내에서 지속적으로 구독·반응하는 상태만 포함한다. |
| `*Mutation` | 상태 변경의 의미 단위 | "상태가 어떻게 바뀌어야 하는가"를 타입으로 드러낸다. `state.copy(isLoading = true)` 대신 `Loading`이라는 의미를 코드에 남긴다. |
| `*SideEffect` | 상태로 보관하지 않는 일회성 동작 | "한 번 소비되면 끝인 이벤트"를 표현. Navigation, Toast, BottomSheet 트리거 등에 사용한다. |

### ViewModel

- feature ViewModel은 반드시 `NeveraViewModel`을 상속한다. `ViewModel()`을 직접 상속하지 않는다.
- 사용자 액션은 `sealed interface *Intent`에 항목을 추가하고, `handleIntent()`를 단일 진입점으로 유지한다. `handleIntent()` 외부에 사용자 액션을 처리하는 `public` 함수를 추가하지 않는다.
- `reduce`는 `applyMutation()` 내부에서만 호출한다. business logic 함수 안에서 직접 호출하지 않는다.

```kotlin
// ✅ 올바른 패턴
private fun onRefreshClicked() = intent {
    applyMutation(HomeMutation.Loading)
}

override suspend fun Syntax<HomeUiState, HomeSideEffect>.applyMutation(mutation: HomeMutation) {
    when (mutation) {
        HomeMutation.Loading -> reduce { state.copy(isLoading = true) }  // ✅ applyMutation 안에서만
    }
}

// ❌ 금지
private fun onRefreshClicked() = intent {
    reduce { state.copy(isLoading = true) }  // ❌ applyMutation 밖에서 reduce 직접 호출
}
```

### AppBar

Scaffold의 `topBar` 파라미터에는 **반드시** 디자인 시스템에서 정의된 AppBar를 사용해야 한다. Material3 기본 AppBar(`TopAppBar`, `CenterAlignedTopAppBar` 등)를 직접 사용하지 않는다.

| 컴포넌트 | 용도 |
|---|---|
| `NeveraAppBar` | 일반 상세·설정 화면. 중앙 제목 + 좌측 navigation + 우측 action |
| `NeveraDisplayAppBar` | 섹션 진입·타이틀 강조 화면. 좌측 큰 제목 + 우측 action |
| `NeveraLogoAppBar` | 메인·브랜드 화면. 좌측 로고 + 우측 action |
| `NeveraSearchAppBar` | 검색 화면. 검색 UI를 AppBar 내에 직접 배치 |

```kotlin
// ✅ 올바른 사용 예시
Scaffold(
    topBar = {
        NeveraAppBar(
            title = "화면 제목",
            navigation = NeveraAppBarNavigation.Back(onClick = onBackClick),
        )
    }
) { ... }

// ❌ 금지
Scaffold(
    topBar = {
        TopAppBar(title = { Text("제목") })       // ❌
        CenterAlignedTopAppBar(title = { ... })   // ❌
    }
)
```

### Screen vs Content 컴포넌트 배치 기준

`*Screen`과 `*Content`는 다음 기준으로 역할을 분리한다.

| 위치 | 역할 | 포함하는 것 |
|---|---|---|
| `*Screen` | 상태 구독, SideEffect 처리, 오케스트레이터 | `collectAsState`, `collectSideEffect`, SideEffect로 트리거되는 컴포넌트, navigation 콜백이 필요한 컴포넌트 |
| `*Content` | UiState의 순수 렌더러 | UiState에서 파생된 모든 UI (Loading, 메인 콘텐츠, UiState-driven 바텀시트) |

> **"이 컴포넌트의 표시 여부가 UiState만으로 결정되는가?"**
> - Yes → `*Content`
> - No (SideEffect + local state, 또는 navigation 콜백 필요) → `*Screen`

```kotlin
// HomeScreen — SideEffect 트리거, navigation 콜백 필요한 것
var showGreetingBottomSheet by remember { mutableStateOf(false) }
viewModel.collectSideEffect { effect ->
    when (effect) {
        HomeSideEffect.ShowGreetingBottomSheet -> showGreetingBottomSheet = true
    }
}
if (showGreetingBottomSheet) { GreetingBottomSheet(...) }              // ✅ Screen에 위치

// HomeContent — UiState만으로 결정되는 것
if (uiState.isLoading) { LoadingIndicator() }                         // ✅ Content에 위치
```

`*Content`의 파라미터는 `uiState: *UiState`와 Intent 람다만 허용한다. local state나 SideEffect에서 파생된 값을 파라미터로 받지 않는다.

```kotlin
// ✅ 올바른 Content 시그니처
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
)

// ❌ 잘못된 Content 시그니처 — local state 혼입 금지
@Composable
fun HomeContent(
    uiState: HomeUiState,
    showGreetingBottomSheet: Boolean,   // ❌
    onGreetingDismiss: () -> Unit,      // ❌
)
```

SideEffect로 트리거된 바텀시트의 dismiss는 `*Screen`의 local state를 직접 `false`로 바꾸는 것으로 처리한다. dismiss를 Intent → ViewModel로 올리지 않는다.

```kotlin
// ✅ dismiss — local state 직접 처리
if (showGreetingBottomSheet) {
    GreetingBottomSheet(
        onSkipClick = { showGreetingBottomSheet = false },
        onDismissRequest = { showGreetingBottomSheet = false },
    )
}

// ❌ dismiss 전용 Intent 금지
onSkipClick = { onIntent(HomeIntent.GreetingSkipClick) }  // ❌
```

### UiState vs SideEffect 선택 기준

| 항목 | UiState | SideEffect |
|---|---|---|
| 성격 | 현재 화면이 어떻게 보여야 하는가 | 한 번 소비되면 끝인 이벤트 |
| 예시 | 세션 전체에서 항상 반응해야 하는 실시간 도메인 상태 | 유저 액션이나 서버 이벤트로 한 번 트리거되는 바텀시트, 토스트, 네비게이션 |
| 판단 기준 | "ViewModel 생존 범위 내에서 구독·반응이 지속되는 상태인가?" | "한 번 소비되면 끝인가?" |

> **참고**: 바텀시트 visibility는 `rememberSaveable`이 Configuration Change를 커버하므로 UiState가 필요 없다. 다른 화면으로 갔다가 돌아올 때 바텀시트가 닫히는 것은 표준 모바일 UX다.

```kotlin
// ✅ UiState — 알림 뱃지는 observeUnreadNotification()으로 세션 내 항상 실시간 반응
val hasUnreadNotification: Boolean = false

// ✅ SideEffect — 서버 상태 체크 후 일회성으로 발화
data object ShowSetNicknameBottomSheet : HomeSideEffect

// ✅ SideEffect — 유저 버튼 탭에 반응하는 일회성 이벤트
data object ShowGreetingBottomSheet : HomeSideEffect
data object ShowCreateWishBottomSheet : HomeSideEffect
```
