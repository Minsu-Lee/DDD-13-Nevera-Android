package com.anddd.nevera.feature.mypage.settingnotification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.feature.mypage.settingnotification.model.SettingNotificationIntent
import com.anddd.nevera.feature.mypage.settingnotification.model.SettingNotificationMutation
import com.anddd.nevera.feature.mypage.settingnotification.model.SettingNotificationSideEffect
import com.anddd.nevera.feature.mypage.settingnotification.model.SettingNotificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class SettingNotificationViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NeveraViewModel<SettingNotificationUiState, SettingNotificationSideEffect, SettingNotificationIntent, SettingNotificationMutation>(
    SettingNotificationUiState()
) {

    private val isSystemNotificationEnabled: Boolean
        get() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    override fun handleIntent(intent: SettingNotificationIntent) {
        when (intent) {
            SettingNotificationIntent.NavigateBack -> intent {
                postSideEffect(SettingNotificationSideEffect.NavigateBack)
            }

            is SettingNotificationIntent.ExpiryAlarmToggled -> onExpiryAlarmToggled(intent.enabled)

            SettingNotificationIntent.AlarmTimeClicked -> intent {
                postSideEffect(SettingNotificationSideEffect.ShowTimePickerDialog)
            }

            is SettingNotificationIntent.AlarmTimeSelected -> intent {
                applyMutation(SettingNotificationMutation.AlarmTimeUpdated(intent.hour, intent.minute))
            }

            SettingNotificationIntent.NavigateToNotificationSettingsClicked -> intent {
                postSideEffect(SettingNotificationSideEffect.OpenNotificationSettings)
            }
        }
    }

    private fun onExpiryAlarmToggled(enabled: Boolean) = intent {
        when {
            enabled && !isSystemNotificationEnabled ->
                postSideEffect(SettingNotificationSideEffect.ShowPermissionDeniedDialog)
            else ->
                applyMutation(SettingNotificationMutation.ExpiryAlarmUpdated(enabled))
        }
    }

    override suspend fun Syntax<SettingNotificationUiState, SettingNotificationSideEffect>.applyMutation(
        mutation: SettingNotificationMutation,
    ) {
        when (mutation) {
            is SettingNotificationMutation.ExpiryAlarmUpdated ->
                reduce { state.copy(isExpiryAlarmEnabled = mutation.enabled) }

            is SettingNotificationMutation.AlarmTimeUpdated ->
                reduce { state.copy(alarmHour = mutation.hour, alarmMinute = mutation.minute) }
        }
    }
}
