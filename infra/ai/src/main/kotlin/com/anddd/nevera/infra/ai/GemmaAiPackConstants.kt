package com.anddd.nevera.infra.ai

internal object GemmaAiPackConstants {
    const val MODEL_FILE_NAME = "gemma4-e2b-it.litertlm"

    val PACK_NAMES = listOf(
        "gemma4_e2b_pack_01",
        "gemma4_e2b_pack_02",
        "gemma4_e2b_pack_03",
    )

    val PARTS = listOf(
        GemmaModelPart("gemma4_e2b_pack_01", "gemma4/gemma4-e2b-it.litertlm.part01", 1),
        GemmaModelPart("gemma4_e2b_pack_02", "gemma4/gemma4-e2b-it.litertlm.part02", 2),
        GemmaModelPart("gemma4_e2b_pack_03", "gemma4/gemma4-e2b-it.litertlm.part03", 3),
    )

    // Fill in after running prepare_gemma4_e2b_model.sh
    // null means skip checksum verification (size-only check)
    val EXPECTED_FULL_SHA256: String? = null
    val EXPECTED_PART_SHA256: List<String?> = listOf(null, null, null)
}
