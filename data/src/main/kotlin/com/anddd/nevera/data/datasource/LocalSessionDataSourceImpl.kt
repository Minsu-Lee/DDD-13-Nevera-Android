package com.anddd.nevera.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anddd.nevera.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

internal class LocalSessionDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SessionDataSource {

    private val dataStore = context.sessionDataStore

    override suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun getSession(): Session {
        val prefs = dataStore.data.first()
        return Session(
            accessToken = prefs[KEY_TOKEN],
            refreshToken = prefs[KEY_REFRESH_TOKEN],
            userId = prefs[KEY_USER_ID]
        )
    }

    override suspend fun getToken(): String? =
        dataStore.data.first()[KEY_TOKEN]

    override suspend fun getRefreshToken(): String? =
        dataStore.data.first()[KEY_REFRESH_TOKEN]

    override suspend fun getUserId(): String? =
        dataStore.data.first()[KEY_USER_ID]

    override suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }
}
