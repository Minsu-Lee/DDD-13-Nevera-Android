---
name: sync-develop
description: upstream의 develop·main 브랜치를 로컬에 merge하고 origin에 자동 push합니다. "upstream sync", "브랜치 동기화", "sync", "최신화" 등의 요청에 응답합니다.
user-invocable: true
---

# sync-develop Skill

## 역할

upstream(팀 원본 저장소)의 `develop` / `main` 브랜치 최신 변경사항을 로컬에 merge하고 origin(개인 fork)에 자동 push한다.

---

## 실행 절차

### Step 1: 현재 브랜치 저장 + 미커밋 변경사항 stash

```bash
git branch --show-current
git status --porcelain
```

- `ORIGINAL_BRANCH`에 현재 브랜치명을 저장한다.
- 출력이 비어있지 않으면(staged/unstaged 변경사항 있음) stash한다:

```bash
git stash push -m "sync-develop auto stash"
```

- stash 실행 여부를 `STASHED=true/false`로 기록해 Step 6에서 복원 여부를 결정한다.

---

### Step 2: Remote 감지

```bash
git remote
```

- `upstream`이 있으면 `REMOTE=upstream`
- 없으면 `REMOTE=origin` (fallback — 사용자에게 알림)

---

### Step 3: Fetch

```bash
git fetch $REMOTE
```

- 실패(네트워크 오류, 인증 실패 등) 시:
  1. `STASHED=true`이면 `git stash pop` 실행 후 원래 브랜치 복귀
  2. 오류 내용을 출력하고 즉시 중단

---

### Step 4: develop 브랜치 sync

로컬에 `develop` 브랜치가 없으면 이 step을 **skip**하고 결과 요약에 `⚠️ develop 브랜치 없음 (skip)`으로 표시한다.

remote에 `$REMOTE/develop` ref가 없으면 동일하게 skip한다.

```bash
git checkout develop
git merge $REMOTE/develop --ff-only
```

- `--ff-only` 실패(diverged) 시 일반 merge 시도:

```bash
git merge $REMOTE/develop
```

- **merge 충돌 발생 시**:
  1. `git merge --abort`
  2. `STASHED=true`이면 `git stash pop`
  3. `git checkout $ORIGINAL_BRANCH`
  4. 충돌 파일 목록을 출력하고 중단:
     > ❌ develop merge 충돌 발생. 아래 파일을 수동으로 해결한 뒤 다시 시도해주세요.

- merge 성공 시:

```bash
git push origin develop
```

- push 거절 시(origin이 앞서 있는 경우):
  > ❌ origin/develop push 실패. 먼저 `git pull origin develop`을 실행해 origin 변경사항을 통합해주세요.
  - 중단 (stash pop + 원래 브랜치 복귀)

- "Already up to date"이면 정상 진행 (push 불필요하면 skip)

---

### Step 5: main 브랜치 sync

로컬에 `main` 브랜치가 없으면 이 step을 **skip**하고 결과 요약에 `⚠️ main 브랜치 없음 (skip)`으로 표시한다.

remote에 `$REMOTE/main` ref가 없으면 동일하게 skip한다.

```bash
git checkout main
git merge $REMOTE/main --ff-only
```

- `--ff-only` 실패 시 일반 merge 시도:

```bash
git merge $REMOTE/main
```

- **충돌 발생 시**: Step 4와 동일한 처리 (abort → stash pop → 원래 브랜치 복귀 → 중단)

- merge 성공 시:

```bash
git push origin main
```

- push 거절 시: Step 4와 동일한 안내 후 중단

---

### Step 6: 원래 브랜치 복귀 + stash 복원

```bash
git checkout $ORIGINAL_BRANCH
```

`STASHED=true`이면:

```bash
git stash pop
```

- `stash pop` 충돌 발생 시:
  - 충돌 파일 목록 출력
  - > ⚠️ stash pop 충돌 발생. 아래 파일을 수동으로 해결해주세요.
  - (스킬 종료 — sync 자체는 완료된 상태)

---

### 완료 메시지

```
✅ Sync 완료 ($REMOTE 기준)

  develop : $REMOTE/develop → merge → push origin ✓  (또는 ⚠️ skip)
  main    : $REMOTE/main    → merge → push origin ✓  (또는 ⚠️ skip)

현재 브랜치: $ORIGINAL_BRANCH
stash 복원 완료 ✓  ← STASHED=true일 때만 표시
```

---

## 예외 처리 요약

| 상황 | 동작 |
|------|------|
| 미커밋 변경사항 있음 | `git stash` 후 진행, 완료 후 `git stash pop` 복원 |
| `stash pop` 충돌 | 충돌 파일 목록 출력 + 수동 해결 안내 (sync는 완료 상태) |
| `upstream` remote 없음 | `origin`으로 fallback, 사용자에게 알림 |
| fetch 실패 | stash 복원 후 즉시 중단 + 오류 메시지 |
| merge 충돌 | `merge --abort` → stash pop → 원래 브랜치 복귀 → 중단 |
| 로컬에 develop/main 없음 | 해당 브랜치 skip + ⚠️ 표시 |
| remote에 develop/main ref 없음 | 해당 브랜치 skip + ⚠️ 표시 |
| push 거절 | 중단 + `git pull origin <branch>` 먼저 실행 안내 |
| 이미 최신 상태 | "Already up to date" 출력 후 정상 진행 |
| 현재 브랜치가 develop 또는 main | stash 불필요, sync 후 동일 브랜치 유지 |
