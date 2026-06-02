#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./install_gemma4_e2b_model_to_device.sh [path-to-model.litertlm]
#   ./install_gemma4_e2b_model_to_device.sh --from-shards
#
# Installs the merged Gemma4 E2B LiteRT-LM model into the debug app's
# noBackupFilesDir so local USB-installed builds can run inference without
# Play AI Delivery.
#
# Target in app sandbox:
#   no_backup/gemma4/gemma4-e2b-it.litertlm
#
# Options:
#   --adb <path>       adb executable path. Defaults to ANDROID_HOME,
#                      ANDROID_SDK_ROOT, ~/Library/Android/sdk, or PATH.
#   --package <name>   Android application id. Defaults to com.anddd.nevera.
#   --from-shards      Merge existing AI Pack shards from the repo first.
#   -h, --help         Print usage.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

MODEL_NAME="gemma4-e2b-it.litertlm"
PACKAGE_NAME="${PACKAGE_NAME:-com.anddd.nevera}"
ADB_PATH="${ADB:-}"
MODEL_PATH=""
USE_SHARDS=false

MANIFEST_FILE="$REPO_ROOT/infra/ai/model-artifacts/gemma4_e2b_manifest.json"
TARGET_DIR="no_backup/gemma4"
TARGET_FILE="$TARGET_DIR/$MODEL_NAME"
TARGET_TMP="$TARGET_FILE.tmp"

PART_FILES=(
    "$REPO_ROOT/gemma4_e2b_pack_01/src/main/assets/gemma4/$MODEL_NAME.part01"
    "$REPO_ROOT/gemma4_e2b_pack_02/src/main/assets/gemma4/$MODEL_NAME.part02"
    "$REPO_ROOT/gemma4_e2b_pack_03/src/main/assets/gemma4/$MODEL_NAME.part03"
)

TMP_DIR=""

usage() {
    cat <<'EOF'
Usage:
  ./install_gemma4_e2b_model_to_device.sh [path-to-model.litertlm]
  ./install_gemma4_e2b_model_to_device.sh --from-shards

Installs the merged Gemma4 E2B LiteRT-LM model into the debug app's
noBackupFilesDir so local USB-installed builds can run inference without
Play AI Delivery.

Target in app sandbox:
  no_backup/gemma4/gemma4-e2b-it.litertlm

Options:
  --adb <path>       adb executable path. Defaults to ANDROID_HOME,
                     ANDROID_SDK_ROOT, ~/Library/Android/sdk, or PATH.
  --package <name>   Android application id. Defaults to com.anddd.nevera.
  --from-shards      Merge existing AI Pack shards from the repo first.
  -h, --help         Print usage.
EOF
}

cleanup() {
    if [[ -n "$TMP_DIR" && -d "$TMP_DIR" ]]; then
        rm -rf "$TMP_DIR"
    fi
}
trap cleanup EXIT

fail() {
    echo "ERROR: $*" >&2
    exit 1
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --adb)
            [[ $# -ge 2 ]] || fail "--adb requires a path"
            ADB_PATH="$2"
            shift 2
            ;;
        --package)
            [[ $# -ge 2 ]] || fail "--package requires an application id"
            PACKAGE_NAME="$2"
            shift 2
            ;;
        --from-shards)
            USE_SHARDS=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        -*)
            fail "Unknown option: $1"
            ;;
        *)
            [[ -z "$MODEL_PATH" ]] || fail "Only one model path can be provided"
            MODEL_PATH="$1"
            shift
            ;;
    esac
done

if [[ -z "$MODEL_PATH" && "$USE_SHARDS" == false ]]; then
    usage >&2
    fail "Provide a model path or pass --from-shards"
fi

if [[ -n "$MODEL_PATH" && "$USE_SHARDS" == true ]]; then
    fail "Use either a model path or --from-shards, not both"
fi

resolve_adb() {
    if [[ -n "$ADB_PATH" ]]; then
        [[ -x "$ADB_PATH" ]] || fail "adb is not executable: $ADB_PATH"
        echo "$ADB_PATH"
        return
    fi

    local candidates=()
    if [[ -n "${ANDROID_HOME:-}" ]]; then
        candidates+=("$ANDROID_HOME/platform-tools/adb")
    fi
    if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
        candidates+=("$ANDROID_SDK_ROOT/platform-tools/adb")
    fi
    candidates+=("$HOME/Library/Android/sdk/platform-tools/adb")

    local candidate
    for candidate in "${candidates[@]}"; do
        if [[ -x "$candidate" ]]; then
            echo "$candidate"
            return
        fi
    done

    if command -v adb >/dev/null 2>&1; then
        command -v adb
        return
    fi

    fail "adb not found. Pass --adb /path/to/adb or set ANDROID_HOME."
}

file_size() {
    if stat -f%z "$1" >/dev/null 2>&1; then
        stat -f%z "$1"
    else
        stat -c%s "$1"
    fi
}

sha256() {
    shasum -a 256 "$1" | awk '{print $1}'
}

expected_manifest_sha() {
    if [[ -f "$MANIFEST_FILE" ]]; then
        awk -F'"' '/"sourceSha256"/ { print $4; exit }' "$MANIFEST_FILE"
    fi
}

merge_shards_if_needed() {
    if [[ -n "$MODEL_PATH" && "$USE_SHARDS" == false ]]; then
        [[ -f "$MODEL_PATH" ]] || fail "Model file does not exist: $MODEL_PATH"
        return
    fi

    local part
    for part in "${PART_FILES[@]}"; do
        [[ -f "$part" ]] || fail "Shard file does not exist: $part"
    done

    TMP_DIR="$(mktemp -d)"
    MODEL_PATH="$TMP_DIR/$MODEL_NAME"

    echo "Merging AI Pack shards into temporary model..."
    cat "${PART_FILES[@]}" > "$MODEL_PATH"
}

install_model() {
    local adb_bin="$1"
    local local_size="$2"

    echo "Checking connected device..."
    "$adb_bin" get-state >/dev/null

    echo "Checking run-as access for $PACKAGE_NAME..."
    "$adb_bin" shell run-as "$PACKAGE_NAME" pwd >/dev/null

    echo "Creating target directory: $TARGET_DIR"
    "$adb_bin" shell run-as "$PACKAGE_NAME" mkdir -p "$TARGET_DIR"
    "$adb_bin" shell run-as "$PACKAGE_NAME" rm -f "$TARGET_TMP"

    echo "Streaming model into app sandbox. This can take several minutes..."
    local stream_status=0
    "$adb_bin" shell run-as "$PACKAGE_NAME" dd "of=$TARGET_TMP" bs=1048576 < "$MODEL_PATH" || stream_status=$?
    if [[ "$stream_status" -ne 0 ]]; then
        echo "WARN: stream command exited with $stream_status; validating copied file before failing." >&2
    fi

    local remote_size
    remote_size="$("$adb_bin" shell run-as "$PACKAGE_NAME" wc -c "$TARGET_TMP" | awk '{print $1}' | tr -d '\r')"
    if [[ "$remote_size" != "$local_size" ]]; then
        "$adb_bin" shell run-as "$PACKAGE_NAME" rm -f "$TARGET_TMP" >/dev/null 2>&1 || true
        fail "Size mismatch after copy. local=$local_size remote=$remote_size"
    fi

    "$adb_bin" shell run-as "$PACKAGE_NAME" mv "$TARGET_TMP" "$TARGET_FILE"
    "$adb_bin" shell run-as "$PACKAGE_NAME" ls -lh "$TARGET_DIR"
}

merge_shards_if_needed

ADB_BIN="$(resolve_adb)"
LOCAL_SIZE="$(file_size "$MODEL_PATH")"
LOCAL_SHA="$(sha256 "$MODEL_PATH")"
EXPECTED_SHA="$(expected_manifest_sha)"

echo "=== Gemma4 E2B Local Device Install ==="
echo "Package: $PACKAGE_NAME"
echo "adb: $ADB_BIN"
echo "Model: $MODEL_PATH"
echo "Size: $LOCAL_SIZE bytes"
echo "SHA-256: $LOCAL_SHA"

if [[ -n "$EXPECTED_SHA" && "$LOCAL_SHA" != "$EXPECTED_SHA" ]]; then
    fail "Model SHA-256 does not match manifest. expected=$EXPECTED_SHA actual=$LOCAL_SHA"
fi

install_model "$ADB_BIN" "$LOCAL_SIZE"

echo "Installed: $TARGET_FILE"
echo "=== Done ==="
