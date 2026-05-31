package com.anddd.nevera.infra.ai.repository

import android.content.Context
import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.google.android.play.core.aipacks.AiPackState
import com.google.android.play.core.aipacks.AiPackStateUpdateListener
import com.google.android.play.core.aipacks.AiPackStates
import com.google.android.play.core.aipacks.model.AiPackErrorCode
import com.google.android.play.core.aipacks.model.AiPackStatus
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class GemmaModelRepositoryTest {

    private lateinit var tmpDir: File
    private lateinit var context: Context
    private lateinit var dataSource: PlayAiPackDataSource
    private lateinit var repository: GemmaModelRepositoryImpl

    @BeforeEach
    fun setUp() {
        tmpDir = Files.createTempDirectory("repo_test").toFile()
        context = mockk {
            every { noBackupFilesDir } returns tmpDir
        }
        dataSource = mockk(relaxed = true)
        repository = GemmaModelRepositoryImpl(context, dataSource)
    }

    @AfterEach
    fun tearDown() {
        tmpDir.deleteRecursively()
    }

    // ── merged model already valid ───────────────────────────────────────────

    @Test
    fun `이미 merge된 모델이 유효하면 fetch 없이 Ready를 emit한다`() = runTest {
        val modelDir = File(tmpDir, "gemma4").apply { mkdirs() }
        File(modelDir, "gemma4-e2b-it.litertlm").writeBytes(ByteArray(10) { 1 })

        repository.requestGemmaModelDownload()

        val state = repository.observeGemmaModelState().first()
        assertTrue(state is GemmaModelState.Ready)
        verify(exactly = 0) { dataSource.registerListener(any()) }
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
        assertEquals(0.25f, state.percent)
    }

    @Test
    fun `totalBytes가 0일 때 percent는 0f이다`() = runTest {
        val packState = mockPackState(AiPackStatus.DOWNLOADING, bytesDownloaded = 0L, totalBytes = 0L)
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(packState)

        repository.requestGemmaModelDownload()

        val state = repository.observeGemmaModelState().first() as GemmaModelState.Downloading
        assertEquals(0f, state.percent)
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
            mockPackState(AiPackStatus.FAILED, errorCode = AiPackErrorCode.NETWORK_ERROR)
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

    // ── listener state update ────────────────────────────────────────────────

    @Test
    fun `listener를 통한 WAITING_FOR_WIFI 콜백은 WaitingForWifi를 emit한다`() = runTest {
        val listenerSlot = slot<AiPackStateUpdateListener>()
        every { dataSource.registerListener(capture(listenerSlot)) } returns Unit
        coEvery { dataSource.fetch(any()) } returns mockAiPackStates(mockPackState(AiPackStatus.PENDING))

        repository.requestGemmaModelDownload()

        // listener receives single AiPackState per callback
        listenerSlot.captured.onStateUpdate(mockPackState(AiPackStatus.WAITING_FOR_WIFI))

        assertEquals(GemmaModelState.WaitingForWifi, repository.observeGemmaModelState().first())
    }

    // ── cancel flow ──────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelGemmaModelDownload 호출 시 Canceling을 먼저 emit하고 이후 Canceled를 emit한다`() =
        // UnconfinedTestDispatcher: 상태 변경 시 collector가 즉시(eager) 실행되어 두 중간 상태를 모두 캡처
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
}
