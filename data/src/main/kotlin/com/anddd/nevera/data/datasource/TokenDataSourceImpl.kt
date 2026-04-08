package com.anddd.nevera.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anddd.nevera.domain.model.LoginProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_token")

internal class TokenDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cryptoHelper: CryptoHelper
) : TokenDataSource {

    private val dataStore = context.tokenDataStore

    override suspend fun getAccessToken(): String? =
        dataStore.data.first()[KEY_ACCESS_TOKEN]?.let(cryptoHelper::decrypt)

    override suspend fun setAccessToken(accessToken: String) {
        dataStore.edit {
            it[KEY_ACCESS_TOKEN] = cryptoHelper.encrypt(accessToken)
        }
    }

    override suspend fun getRefreshToken(): String? =
        dataStore.data.first()[KEY_REFRESH_TOKEN]?.let(cryptoHelper::decrypt)

    override suspend fun setRefreshToken(refreshToken: String) {
        dataStore.edit {
            it[KEY_REFRESH_TOKEN] = cryptoHelper.encrypt(refreshToken)
        }
    }

    override suspend fun setTokens(accessToken: String, refreshToken: String) {
        dataStore.edit {
            it[KEY_ACCESS_TOKEN] = cryptoHelper.encrypt(accessToken)
            it[KEY_REFRESH_TOKEN] = cryptoHelper.encrypt(refreshToken)
        }
    }

    override suspend fun getProvider(): LoginProvider? {
        val providerName = dataStore.data.first()[KEY_LOGIN_PROVIDER]?.let(cryptoHelper::decrypt)
        return LoginProvider.toProvider(providerName)
    }

    override suspend fun setProvider(provider: LoginProvider) {
        dataStore.edit { it[KEY_LOGIN_PROVIDER] = cryptoHelper.encrypt(provider.providerName) }
    }

    override suspend fun clearLoginData() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_LOGIN_PROVIDER = stringPreferencesKey("login_provider")
    }
}
