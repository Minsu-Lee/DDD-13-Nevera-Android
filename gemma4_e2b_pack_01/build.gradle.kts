plugins {
    id("com.android.ai-pack")
}

aiPack {
    packName = "gemma4_e2b_pack_01"
    dynamicDelivery {
        deliveryType = "on-demand"
    }
}
