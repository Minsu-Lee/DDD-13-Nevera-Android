package com.anddd.nevera

import timber.log.Timber

object TimberInitializer {

    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}
