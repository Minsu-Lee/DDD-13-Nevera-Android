package com.anddd.nevera.infra.ai.repository

import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.anddd.nevera.infra.ai.merger.ShardMerger
import com.google.android.play.core.aipacks.AiPackState
import com.google.android.play.core.aipacks.AiPackStateUpdateListener
import com.google.android.play.core.aipacks.AiPackStates
import com.google.android.play.core.aipacks.model.AiPackErrorCode
import com.google.android.play.core.aipacks.model.AiPackStatus
import com.google.android.play.core.assetpacks.AssetPackException
import com.google.android.play.core.assetpacks.model.AssetPackErrorCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GemmaModelRepositoryTest {

    private lateinit var dataSource: PlayAiPackDataSource
    private lateinit var merger: ShardMerger
    private lateinit var repository: GemmaModelRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = mockk(relaxed = true)
        merger = mockk(relaxed = true) {
            every { isModelReady() } returns false
            every { modelPath() } returns "/fake/model/path"
        }
        repository = GemmaModelRepositoryImpl(dataSource, merger)
    }

    // ── merged model already valid ───────────────────────────────────────────

    @Test
    fun `이미 merge된 모델이 유효하면 fetch 없이 Ready를 emit한다`() = runTest {
        every { merger.isModelReady() } returns true

        repository.requestGemmaModelDownload()

        val state = repository.observeGemmaModelState().first()
        assertTrue(state is GemmaModelState.Ready)
        assertEquals("/fake/model/path", (state as GemmaModelState.Ready).modelPath)
        verify(exactly = 0) { dataSource.registerListener(any()) }
    }

    @Test
    fun `refresh 시 현재 pack 상태가 REQUIRES_USER_CONFIRMATION이면 RequiresUserConfirmation으로 매핑한다`() = runTest {
        coEvery { dataSource.getPackStates(any()) } returns mockAiPackStates(
            mockPackState(AiPackStatus.REQUIRES_USER_CONFIRMATION),
        )

        val state = repository.refreshGemmaModelState()

        assertEquals(GemmaModelState.RequiresUserConfirmation, state)
        assertEquals(GemmaModelState.RequiresUserConfirmation, repository.observeGemmaModelState().first())
        verify { dataSource.registerListener(any()) }
    }

    // ── download percent calculation ─────────────────────────────────────────

    @Test
    fun `DOWNLOADING 상태에서 pack별 bytes를 합산해 percent를 계산한다`() = runTest {
        val packState1 = mockPackState(AiPackStatus.DOWNLOADING, bytesDownloaded = 300L, totalBytes = 1000L)
        val packState2 = mockPackState(AiPackStatus.DOWNLOADING, bytesDownloaded = 200L, totalBytes = 1000L, packName = "gemma4_e2b_pack_02")
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(packState1, packState2)

        repository.requestGemmaModelDownload()

        val state = repository.observeGemmaModelState().first() as GemmaModelState.Downloading
        assertEquals(500L, state.bytesDownloaded)
        assertEquals(2000L, state.totalBytes)
        assertEquals(0.25f, state.percent, 0.0001f)
    }

    @Test
    fun `totalBytes가 0일 때 percent는 0f이다`() = runTest {
        val packState = mockPackState(AiPackStatus.DOWNLOADING, bytesDownloaded = 0L, totalBytes = 0L)
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(packState)

        repository.requestGemmaModelDownload()

        val state = repository.observeGemmaModelState().first() as GemmaModelState.Downloading
        assertEquals(0f, state.percent, 0.0001f)
    }

    // ── status mappings ──────────────────────────────────────────────────────

    @Test
    fun `PENDING 상태는 Pending으로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.PENDING))
        repository.requestGemmaModelDownload()

        assertEquals(GemmaModelState.Pending, repository.observeGemmaModelState().first())
    }

    @Test
    fun `WAITING_FOR_WIFI 상태는 WaitingForWifi로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.WAITING_FOR_WIFI))
        repository.requestGemmaModelDownload()

        assertEquals(GemmaModelState.WaitingForWifi, repository.observeGemmaModelState().first())
    }

    @Test
    fun `REQUIRES_USER_CONFIRMATION 상태는 RequiresUserConfirmation으로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.REQUIRES_USER_CONFIRMATION))
        repository.requestGemmaModelDownload()

        assertEquals(
            GemmaModelState.RequiresUserConfirmation,
            repository.observeGemmaModelState().first(),
        )
    }

    @Test
    fun `FAILED 상태는 Failed(PlayError)로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(
            mockPackState(AiPackStatus.FAILED, errorCode = AiPackErrorCode.NETWORK_ERROR),
        )
        repository.requestGemmaModelDownload()

        assertEquals(
            GemmaModelState.Failed(GemmaModelError.PlayError(AiPackErrorCode.NETWORK_ERROR)),
            repository.observeGemmaModelState().first(),
        )
    }

    @Test
    fun `UNKNOWN 상태는 Failed(UnknownPack)으로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.UNKNOWN))
        repository.requestGemmaModelDownload()

        assertEquals(
            GemmaModelState.Failed(GemmaModelError.UnknownPack),
            repository.observeGemmaModelState().first(),
        )
    }

    @Test
    fun `CANCELED 상태는 Canceled로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.CANCELED))
        repository.requestGemmaModelDownload()

        assertEquals(GemmaModelState.Canceled, repository.observeGemmaModelState().first())
    }

    @Test
    fun `NOT_INSTALLED 상태는 NotInstalled로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.NOT_INSTALLED))
        repository.requestGemmaModelDownload()

        assertEquals(GemmaModelState.NotInstalled, repository.observeGemmaModelState().first())
    }

    @Test
    fun `fetch 예외 발생 시 Failed(Unknown)으로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } throws RuntimeException("network error")
        repository.requestGemmaModelDownload()

        assertTrue(repository.observeGemmaModelState().first() is GemmaModelState.Failed)
    }

    @Test
    fun `fetch에서 APP_NOT_OWNED 예외 발생 시 AppNotOwned로 매핑된다`() = runTest {
        coEvery { dataSource.fetch(any()) } throws assetPackException(AssetPackErrorCode.APP_NOT_OWNED)

        repository.requestGemmaModelDownload()

        assertEquals(
            GemmaModelState.Failed(GemmaModelError.AppNotOwned),
            repository.observeGemmaModelState().first(),
        )
    }

    // ── listener state update ────────────────────────────────────────────────

    @Test
    fun `listener를 통한 WAITING_FOR_WIFI 콜백은 WaitingForWifi를 emit한다`() = runTest {
        val listenerSlot = slot<AiPackStateUpdateListener>()
        every { dataSource.registerListener(capture(listenerSlot)) } returns Unit
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.PENDING))

        repository.requestGemmaModelDownload()

        listenerSlot.captured.onStateUpdate(mockPackState(AiPackStatus.WAITING_FOR_WIFI))

        assertEquals(GemmaModelState.WaitingForWifi, repository.observeGemmaModelState().first())
    }

    // ── cancel flow ──────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelGemmaModelDownload 호출 시 Canceling을 먼저 emit하고 이후 Canceled를 emit한다`() =
        runTest(UnconfinedTestDispatcher()) {
            val emittedStates = mutableListOf<GemmaModelState>()
            val job = launch {
                repository.observeGemmaModelState().collect { emittedStates.add(it) }
            }

            every { dataSource.cancel(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.CANCELED))
            repository.cancelGemmaModelDownload()
            job.cancel()

            assertTrue(emittedStates.contains(GemmaModelState.Canceling))
            assertTrue(emittedStates.contains(GemmaModelState.Canceled))
        }

    @Test
    fun `cancel 후 requestGemmaModelDownload를 다시 호출할 수 있다`() = runTest {
        every { dataSource.cancel(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.CANCELED))
        repository.cancelGemmaModelDownload()

        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.PENDING))
        repository.requestGemmaModelDownload()

        assertEquals(GemmaModelState.Pending, repository.observeGemmaModelState().first())
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun mockPackState(
        status: Int,
        bytesDownloaded: Long = 0L,
        totalBytes: Long = 0L,
        errorCode: Int = AiPackErrorCode.NO_ERROR,
        packName: String = "gemma4_e2b_pack_01",
    ): AiPackState = mockk {
        every { this@mockk.status() } returns status
        every { this@mockk.bytesDownloaded() } returns bytesDownloaded
        every { this@mockk.totalBytesToDownload() } returns totalBytes
        every { this@mockk.errorCode() } returns errorCode
        every { this@mockk.name() } returns packName
        every { this@mockk.transferProgressPercentage() } returns 0
    }

    private fun mockAiPackStates(vararg states: AiPackState): AiPackStates {
        val statesMap = states.associateBy { it.name() }
        return mockk { every { packStates() } returns statesMap }
    }

    private fun assetPackException(errorCode: Int): AssetPackException {
        val constructor = AssetPackException::class.java.getDeclaredConstructor(Int::class.javaPrimitiveType)
        constructor.isAccessible = true
        return constructor.newInstance(errorCode)
    }
}
