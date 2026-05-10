package app.mori.reader.features.anki.presentation

import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDeck
import app.mori.reader.data.anki.AnkiFetchResult
import app.mori.reader.data.anki.AnkiField
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.anki.AnkiMiningContext
import app.mori.reader.data.anki.AnkiNoteType
import app.mori.reader.data.anki.AnkiService
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.anki.AnkiSettingsRepository
import app.mori.reader.data.anki.AnkiShareFallback
import app.mori.reader.data.anki.AnkiTransport
import app.mori.reader.ui.AppEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AnkiViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchClearsStaleMappingsAfterModelChange() =
        runTest {
            val settingsRepository =
                FakeSettingsRepository(
                    AnkiSettings(
                        selectedNoteType = "Old",
                        fieldMappings = mapOf("Expression" to "{expression}", "Obsolete" to "{reading}"),
                    ),
                )
            val repository =
                FakeAnkiTransport(
                    fetchResult =
                        AnkiFetchResult(
                            decks = listOf(AnkiDeck(id = "1", name = "Mining")),
                            noteTypes =
                                listOf(
                                    AnkiNoteType(
                                        id = "2",
                                        name = "New",
                                        fields = listOf(AnkiField("Expression"), AnkiField("Reading")),
                                    ),
                                ),
                        ),
                )
            val viewModel = AnkiViewModel(settingsRepository, service(settingsRepository, repository))
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntent(AnkiIntent.FetchDecksAndModels)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("New", settingsRepository.current.selectedNoteType)
            assertEquals(mapOf("Expression" to "{expression}", "Reading" to ""), settingsRepository.current.fieldMappings)
        }

    @Test
    fun failedPingSetsDisconnected() =
        runTest {
            val settingsRepository =
                FakeSettingsRepository(AnkiSettings(connectionMode = AnkiConnectionMode.AnkiConnect))
            val viewModel =
                AnkiViewModel(
                    settingsRepository,
                    service(
                        settingsRepository,
                        FakeAnkiTransport(mode = AnkiConnectionMode.AnkiConnect, pingResult = false),
                    ),
                )
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntent(AnkiIntent.Connect)
            dispatcher.scheduler.advanceUntilIdle()

            assertFalse(viewModel.state.value.isConnected)
        }

    @Test
    fun successfulAddSyncsOnlyWhenForced() =
        runTest {
            val settingsRepository = FakeSettingsRepository(AnkiSettings(forceSync = true))
            val repository = FakeAnkiTransport()
            val viewModel = AnkiViewModel(settingsRepository, service(settingsRepository, repository))
            dispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntent(AnkiIntent.MineNote(content(), AnkiMiningContext()))
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, repository.addNoteCalls)
            assertEquals(1, repository.syncCalls)
            assertTrue(viewModel.effects.first() is AppEffect.ShowMessage)
        }

    private fun content(): AnkiMiningContent = AnkiMiningContent(expression = "猫")
}

private fun service(
    settingsRepository: FakeSettingsRepository,
    vararg transports: FakeAnkiTransport,
): AnkiService =
    AnkiService(
        settingsRepository = settingsRepository,
        transports = transports.toList(),
        shareFallback =
            object : AnkiShareFallback {
                override suspend fun share(
                    settings: AnkiSettings,
                    content: AnkiMiningContent,
                    context: AnkiMiningContext,
                ) = Unit
            },
    )

private class FakeAnkiTransport(
    override val mode: AnkiConnectionMode = AnkiConnectionMode.AnkiDroid,
    private val pingResult: Boolean = true,
    private val fetchResult: AnkiFetchResult = AnkiFetchResult(),
) : AnkiTransport {
    var addNoteCalls = 0
    var syncCalls = 0

    override suspend fun ping(settings: AnkiSettings): Boolean = pingResult

    override suspend fun fetchDecksAndModels(settings: AnkiSettings): AnkiFetchResult = fetchResult

    override suspend fun addNote(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        addNoteCalls++
    }

    override suspend fun checkDuplicate(
        settings: AnkiSettings,
        expression: String,
    ): Boolean = false

    override suspend fun sync(settings: AnkiSettings) {
        syncCalls++
    }
}

private class FakeSettingsRepository(
    initial: AnkiSettings,
) : AnkiSettingsRepository {
    private val ankiSettings = MutableStateFlow(initial)
    val current: AnkiSettings
        get() = ankiSettings.value

    override val settingsFlow = ankiSettings

    override suspend fun updateSettings(transform: (AnkiSettings) -> AnkiSettings) {
        ankiSettings.value = transform(ankiSettings.value)
    }
}
