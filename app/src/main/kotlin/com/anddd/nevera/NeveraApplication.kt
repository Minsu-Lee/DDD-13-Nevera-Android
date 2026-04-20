package com.anddd.nevera

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import com.anddd.nevera.core.network.auth.SessionEventBus
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.anddd.nevera.core.notification.R as NotificationR

@HiltAndroidApp
class NeveraApplication : Application() {

    @Inject lateinit var sessionEventBus: SessionEventBus

    // Application 생명주기와 동일한 스코프 (앱 프로세스 종료 시 함께 취소됨)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeSessionExpired()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(NotificationR.string.default_notification_channel_id),
            getString(NotificationR.string.default_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 세션 만료 이벤트를 구독하여 앱 전체를 초기화한다.
     *
     * Application 레벨에서 구독하므로, foreground Activity가 무엇이든 (MainActivity, DetailActivity 등)
     * 관계없이 OS Task 전체를 클리어하고 MainActivity를 재시작한다.
     * MainActivity는 startDestination이 Splash이므로 토큰 없음 → Login으로 이동한다.
     */
    private fun observeSessionExpired() {
        applicationScope.launch {
            sessionEventBus.sessionExpiredEvent.collect {
                Intent(this@NeveraApplication, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }.let(::startActivity)
            }
        }
    }
}
