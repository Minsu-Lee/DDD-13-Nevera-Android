package com.anddd.nevera.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.pushTokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "push_token")

internal class FcmTokenLocalDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
) : FcmTokenLocalDataSource {

    private val dataStore = context.pushTokenDataStore

    override suspend fun getFcmToken(): String? =
        dataStore.data.first()[KEY_FCM_TOKEN]

    override suspend fun isSyncNeeded(): Boolean =
        dataStore.data.first()[KEY_NEEDS_SYNC] ?: false

    override suspend fun setNeedsSync(value: Boolean) {
        dataStore.edit { it[KEY_NEEDS_SYNC] = value }
    }

    override suspend fun clearFcmData() {
        dataStore.edit { it.clear() }
    }

    // DataStore.edit는 atomic transaction — 토큰과 sync 상태를 동시에 업데이트해 불일치를 방지한다.
    override suspend fun saveTokenPendingSync(token: String) {
        dataStore.edit {
            it[KEY_FCM_TOKEN] = token
            it[KEY_NEEDS_SYNC] = true
        }
    }

    companion object {
        private val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val KEY_NEEDS_SYNC = booleanPreferencesKey("fcm_token_needs_sync")
    }
}
