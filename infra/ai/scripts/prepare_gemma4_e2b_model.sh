#!/usr/bin/env bash
set -euo pipefail

# Usage: ./prepare_gemma4_e2b_model.sh <path-to-model.litertlm>
#
# Splits the Gemma4 E2B model into 3 shards and places them in the
# AI Pack asset directories. Generates a manifest JSON at:
#   infra/ai/model-artifacts/gemma4_e2b_manifest.json
#
# Requirements (macOS defaults): shasum, split, cmp

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

MODEL_PATH="${1:-}"
if [[ -z "$MODEL_PATH" || ! -f "$MODEL_PATH" ]]; then
    echo "ERROR: Provide a valid path to the .litertlm model file." >&2
    echo "Usage: $0 <path-to-model.litertlm>" >&2
    exit 1
fi

MODEL_NAME="gemma4-e2b-it.litertlm"
MAX_SHARD_BYTES=1200000000  # 1.2 GB
MANIFEST_DIR="$REPO_ROOT/infra/ai/model-artifacts"
MANIFEST_FILE="$MANIFEST_DIR/gemma4_e2b_manifest.json"

PACK_DIRS=(
    "$REPO_ROOT/gemma4_e2b_pack_01/src/main/assets/gemma4"
    "$REPO_ROOT/gemma4_e2b_pack_02/src/main/assets/gemma4"
    "$REPO_ROOT/gemma4_e2b_pack_03/src/main/assets/gemma4"
)
PACK_NAMES=("gemma4_e2b_pack_01" "gemma4_e2b_pack_02" "gemma4_e2b_pack_03")
PART_SUFFIXES=("part01" "part02" "part03")

echo "=== Gemma4 E2B Model Preparation ==="
echo "Source: $MODEL_PATH"

SOURCE_SIZE=$(stat -f%z "$MODEL_PATH")
echo "Source size: $SOURCE_SIZE bytes"

SOURCE_SHA256=$(shasum -a 256 "$MODEL_PATH" | awk '{print $1}')
echo "Source SHA-256: $SOURCE_SHA256"

# Split model into shards
TMP_SPLIT_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_SPLIT_DIR"' EXIT

echo "Splitting model into shards (max $MAX_SHARD_BYTES bytes each)..."
split -b "$MAX_SHARD_BYTES" "$MODEL_PATH" "$TMP_SPLIT_DIR/shard_"

SHARD_FILES=($(ls "$TMP_SPLIT_DIR"/shard_* | sort))
SHARD_COUNT="${#SHARD_FILES[@]}"

if [[ "$SHARD_COUNT" -gt 3 ]]; then
    echo "ERROR: Model split into $SHARD_COUNT shards, but only 3 AI Pack modules are configured." >&2
    exit 1
fi

echo "Split into $SHARD_COUNT shard(s)."

# Copy shards to AI Pack asset directories
mkdir -p "$MANIFEST_DIR"

PARTS_JSON=""
for i in $(seq 0 $((SHARD_COUNT - 1))); do
    SHARD="${SHARD_FILES[$i]}"
    DEST_DIR="${PACK_DIRS[$i]}"
    PACK_NAME="${PACK_NAMES[$i]}"
    SUFFIX="${PART_SUFFIXES[$i]}"
    DEST_FILE="$DEST_DIR/$MODEL_NAME.$SUFFIX"
    RELATIVE_ASSET="gemma4/$MODEL_NAME.$SUFFIX"

    mkdir -p "$DEST_DIR"
    cp "$SHARD" "$DEST_FILE"
    echo "Copied shard $((i+1)) → $DEST_FILE"

    PART_SIZE=$(stat -f%z "$DEST_FILE")
    PART_SHA256=$(shasum -a 256 "$DEST_FILE" | awk '{print $1}')

    COMMA=""
    [[ -n "$PARTS_JSON" ]] && COMMA=","
    PARTS_JSON="$PARTS_JSON$COMMA
    {
      \"packName\": \"$PACK_NAME\",
      \"relativeAssetPath\": \"$RELATIVE_ASSET\",
      \"sizeBytes\": $PART_SIZE,
      \"sha256\": \"$PART_SHA256\",
      \"order\": $((i+1))
    }"
done

# Verify: merge shards back and compare with original
TMP_MERGED=$(mktemp)
trap 'rm -rf "$TMP_SPLIT_DIR" "$TMP_MERGED"' EXIT

for i in $(seq 0 $((SHARD_COUNT - 1))); do
    DEST_DIR="${PACK_DIRS[$i]}"
    SUFFIX="${PART_SUFFIXES[$i]}"
    cat "$DEST_DIR/$MODEL_NAME.$SUFFIX" >> "$TMP_MERGED"
done

echo "Verifying merged output matches source..."
if ! cmp -s "$MODEL_PATH" "$TMP_MERGED"; then
    echo "ERROR: Merge verification failed — shard content does not match source." >&2
    exit 1
fi
echo "Verification PASSED."

# Write manifest
cat > "$MANIFEST_FILE" <<EOF
{
  "modelName": "gemma4-e2b-it",
  "sourceFileName": "$(basename "$MODEL_PATH")",
  "sourceSizeBytes": $SOURCE_SIZE,
  "sourceSha256": "$SOURCE_SHA256",
  "parts": [$PARTS_JSON
  ]
}
EOF

echo "Manifest written: $MANIFEST_FILE"
echo "=== Done ==="
