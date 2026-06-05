# Nevera Release Drafter 구현 가이드

PR이 develop에 머지될 때마다 Draft Release를 자동으로 생성·갱신하는 Release Drafter 구현을 설명한다.

---

## 파일 구성

| 파일 | 역할 |
|---|---|
| `.github/workflows/release-drafter.yml` | 워크플로우 — 언제 실행할지 |
| `.github/release-drafter.yml` | 설정 — 릴리즈 노트를 어떻게 만들지 |

---

## 워크플로우 `.github/workflows/release-drafter.yml`

```yaml
name: Release Drafter

on:
  push:
    branches:
      - develop

jobs:
  update_release_draft:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      pull-requests: read
    steps:
      - uses: release-drafter/release-drafter@v6
        with:
          config-name: release-drafter.yml
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 트리거

`develop` 브랜치에 push가 발생할 때마다 실행된다. PR이 develop으로 머지되는 것이 push를 발생시키므로, **PR 머지 = Draft Release 갱신**이다.

### 권한

| 권한 | 이유 |
|---|---|
| `contents: write` | Draft Release 생성·수정 |
| `pull-requests: read` | 머지된 PR 목록 및 라벨 조회 |

### 실행 방식

`release-drafter/release-drafter@v6` 액션 하나만 실행한다. 이 액션은 GitHub API만으로 동작하기 때문에 `actions/checkout`이 필요 없다. `config-name: release-drafter.yml`은 기본값이지만 명시적으로 지정해두었다.

---

## 설정 `.github/release-drafter.yml`

### 버전 및 태그 이름

```yaml
name-template: 'v$RESOLVED_VERSION'
tag-template: 'v$RESOLVED_VERSION'
```

Draft Release의 이름과 태그가 `v1.2.3` 형식으로 자동 설정된다. `$RESOLVED_VERSION`은 머지된 PR의 라벨을 기준으로 아래 categories 규칙에 따라 계산된다.

---

### 카테고리

```yaml
categories:
  - title: '✨ 새 기능'
    labels: [feat, feature]

  - title: '🐛 버그 수정'
    labels: [fix, bug]

  - title: '🎨 디자인 변경'
    labels: [design, ui]

  - title: '⚡️ 성능 개선'
    labels: [perf, performance]

  - title: '🔧 내부 작업'
    labels: [chore, refactor, ci, test, docs]

exclude-labels:
  - 'skip-changelog'

version-resolver:
  major:
    labels: [breaking-change]
  minor:
    labels: [feat, feature]
  default: patch
```

각 카테고리는 `labels`에 지정된 라벨을 가진 PR을 묶어 릴리즈 노트 섹션으로 표시한다.

| 카테고리 | 라벨 |
|---|---|
| ✨ 새 기능 | `feat`, `feature` |
| 🐛 버그 수정 | `fix`, `bug` |
| 🎨 디자인 변경 | `design`, `ui` |
| ⚡️ 성능 개선 | `perf`, `performance` |
| 🔧 내부 작업 | `chore`, `refactor`, `ci`, `test`, `docs` |

#### `exclude-labels` — 완전 제외

`skip-changelog` 라벨이 붙은 PR은 릴리즈 노트에 전혀 표시되지 않는다. 버전 계산에도 기여하지 않는다.

#### `version-resolver` — 버전 계산

`categories`와 별도로 버전 증가 규칙을 정의한다.

```yaml
version-resolver:
  major:
    labels: [breaking-change]   # major 증가
  minor:
    labels: [feat, feature]     # minor 증가
  default: patch                # 그 외 모두 patch
```

릴리즈 노트에는 렌더링되지 않고 버전 결정에만 사용된다.

#### 버전 결정 규칙 요약

여러 라벨이 섞여 있을 때는 가장 높은 increment가 선택된다.

| 머지된 PR 라벨 조합 | 최종 버전 증가 |
|---|---|
| `feat` + `fix` | minor (`feat`이 더 높음) |
| `fix` + `design` | patch |
| `breaking-change` + `feat` | major (`breaking-change`가 가장 높음) |
| `chore`만 | patch (default) |
| `skip-changelog`만 | 버전 변화 없음 (제외됨) |

---

### PR 포맷

```yaml
change-template: "- $TITLE @$AUTHOR (#$NUMBER)"
change-title-escapes: '\<*_&'
```

각 PR은 아래 형식으로 한 줄씩 표시된다.

```
- 로그인 화면 디자인 수정 @Jooman-Lee (#42)
```

`change-title-escapes`는 PR 제목에 `\`, `<`, `*`, `_`, `&` 같은 마크다운 특수문자가 포함되었을 때 자동으로 이스케이프해 깨짐을 방지한다.

---

### 릴리즈 노트 템플릿

```yaml
no-changes-template: '변경사항 없음'

template: |
  ## 변경사항

  $CHANGES
```

`$CHANGES`에 카테고리별로 분류된 PR 목록이 삽입된다. 포함할 PR이 하나도 없으면 `변경사항 없음`이 표시된다.

**생성 결과 예시:**

```
## 변경사항

### ✨ 새 기능
- 위시리스트 생성 기능 추가 @Jooman-Lee (#38)

### 🐛 버그 수정
- 로그인 크래시 수정 @Minsu-Lee (#41)

### 🔧 내부 작업
- CI Node.js 24 마이그레이션 @Jooman-Lee (#39)
```

---

## 전체 흐름

```
PR 라벨 부착 (feat, fix, design 등)
    ↓
develop에 PR 머지
    ↓
release-drafter.yml 워크플로우 실행
    ↓
직전 published release 이후 머지된 PR 라벨 스캔
    ↓
├── skip-changelog → pre-exclude (제외)
├── breaking-change → major 버전 증가
├── feat/feature → minor 버전 증가 + ✨ 새 기능 섹션
├── fix/bug → patch 버전 증가 + 🐛 버그 수정 섹션
├── design/ui → patch 버전 증가 + 🎨 디자인 변경 섹션
├── perf/performance → patch 버전 증가 + ⚡️ 성능 개선 섹션
└── chore/refactor/ci/test/docs → patch 버전 증가 + 🔧 내부 작업 섹션
    ↓
Draft Release body 갱신 (GitHub Releases 탭에서 확인 가능)
    ↓
cd-firebase.yml이 이 body를 읽어 Firebase 배포 릴리즈 노트로 사용
```

---

## PR 라벨 부착 규칙

Draft Release가 올바르게 생성되려면 PR에 아래 라벨 중 하나를 반드시 붙여야 한다.

| 라벨 | 용도 |
|---|---|
| `feat` / `feature` | 새 기능 |
| `fix` / `bug` | 버그 수정 |
| `design` / `ui` | UI/UX 변경 |
| `perf` / `performance` | 성능 개선 |
| `chore` | 유지보수, 설정 변경 |
| `refactor` | 코드 구조 개선 |
| `ci` | CI/CD 파이프라인 변경 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 변경 |
| `breaking-change` | 하위 호환 불가 변경 |
| `skip-changelog` | 릴리즈 노트 제외 |

라벨이 없으면 어느 카테고리에도 속하지 않아 `변경사항 없음`으로 표시될 수 있다.
