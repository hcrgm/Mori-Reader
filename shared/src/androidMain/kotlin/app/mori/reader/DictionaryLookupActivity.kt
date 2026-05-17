package app.mori.reader

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.mori.reader.data.anki.AnkiMiningContext
import app.mori.reader.data.anki.AnkiService
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.dictionary.domain.DictionaryLookupUseCase
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import app.mori.reader.features.lookup.presentation.createLookupStackEntry
import app.mori.reader.features.lookup.presentation.dismissLookupStack
import app.mori.reader.features.lookup.presentation.withLookupError
import app.mori.reader.features.lookup.presentation.withLookupResult
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_search_failed
import app.mori.reader.ui.pages.dictionary.DictionaryFirstPopupPlacement
import app.mori.reader.ui.pages.dictionary.DictionaryLookupPopupStack
import app.mori.reader.ui.pages.reader.ReaderSheetTheme
import app.mori.reader.ui.text.UiText
import app.mori.reader.ui.theme.AppTheme
import app.mori.reader.ui.theme.toMoriThemeState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext

class DictionaryLookupActivity : ComponentActivity() {
    private val lookupText: DictionaryLookupUseCase by lazy { GlobalContext.get().get() }
    private val settingsRepository: SettingsRepository by lazy { GlobalContext.get().get() }
    private val ankiService: AnkiService by lazy { GlobalContext.get().get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val query = intent.lookupText()
        if (query.isBlank()) {
            finish()
            return
        }

        Thread {
            val initialSettings = readInitialSettings()
            runOnUiThread {
                setContent {
                    ExternalDictionaryLookup(
                        query = query,
                        initialSettings = initialSettings,
                        lookupText = lookupText,
                        settingsRepository = settingsRepository,
                        ankiService = ankiService,
                        onDismiss = ::finish,
                        onMessage = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }.start()
    }

    private fun readInitialSettings(): AppSettings =
        try {
            runBlocking { settingsRepository.settings.first() }
        } catch (_: Throwable) {
            AppSettings()
        }
}

private fun Intent.lookupText(): String =
    when (action) {
        Intent.ACTION_PROCESS_TEXT -> {
            getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        }

        Intent.ACTION_SEND,
        ACTION_TRANSLATE,
        -> {
            getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty()
        }

        else -> ""
    }.trim()

private const val ACTION_TRANSLATE = "android.intent.action.TRANSLATE"

@Composable
private fun ExternalDictionaryLookup(
    query: String,
    initialSettings: AppSettings,
    lookupText: DictionaryLookupUseCase,
    settingsRepository: SettingsRepository,
    ankiService: AnkiService,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
) {
    var settings by remember { mutableStateOf(initialSettings) }

    LaunchedEffect(settingsRepository) {
        settingsRepository.settings.collect { settings = it }
    }

    AppLocaleEnvironment(mode = settings.appearance.languageMode) {
        ApplyLanguageModeEffect(settings.appearance.languageMode)
        AppTheme(themeState = settings.appearance.toMoriThemeState()) {
            DictionaryLookupPopupContent(
                query = query,
                settings = settings,
                lookupText = lookupText,
                ankiService = ankiService,
                onDismiss = onDismiss,
                onMessage = onMessage,
            )
        }
    }
}

@Composable
private fun DictionaryLookupPopupContent(
    query: String,
    settings: AppSettings,
    lookupText: DictionaryLookupUseCase,
    ankiService: AnkiService,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val searchFailedMessage = stringResource(Res.string.error_search_failed)
    var nextLookupId by remember(query) { mutableIntStateOf(1) }
    var lookupStack by remember(query) {
        mutableStateOf(
            createLookupStackEntry(
                stack = emptyList(),
                parentIndex = null,
                lookupId = 1,
                text = query,
                sentence = query,
                rect = null,
            ),
        )
    }
    var duplicateExpression by remember(query) { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isDark =
        when (settings.appearance.themeMode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    val materialEInkMode =
        settings.appearance.uiThemeEngine == UiThemeEngine.Material &&
            settings.appearance.materialEInkMode

    fun dismissLookup(index: Int?) {
        lookupStack = dismissLookupStack(lookupStack, index)
        if (lookupStack.isEmpty()) {
            onDismiss()
        }
    }

    fun appendLookup(
        parentIndex: Int?,
        text: String,
        rect: ReaderSelectionRect?,
    ) {
        val term = text.trim()
        if (term.isBlank()) return
        duplicateExpression = null
        nextLookupId += 1
        lookupStack =
            createLookupStackEntry(
                stack = lookupStack,
                parentIndex = parentIndex,
                lookupId = nextLookupId,
                text = term,
                sentence = term,
                rect = rect,
            )
    }

    LaunchedEffect(
        lookupStack.map { it.id to it.selectedText },
        settings.dictionary.maxResults,
    ) {
        lookupStack
            .filter { it.isSearching }
            .forEach { pendingLookup ->
                val term = pendingLookup.selectedText.trim()
                if (term.isBlank()) {
                    dismissLookup(lookupStack.indexOfFirst { it.id == pendingLookup.id }.takeIf { it >= 0 })
                    return@forEach
                }
                runCatching { lookupText(term, settings.dictionary.maxResults) }
                    .onSuccess { result ->
                        lookupStack =
                            lookupStack.withLookupResult(
                                lookupId = pendingLookup.id,
                                entries = result.entries,
                                dictionaryStyles = result.styles,
                            )
                    }.onFailure { throwable ->
                        lookupStack =
                            lookupStack.withLookupError(
                                lookupId = pendingLookup.id,
                                errorMessage = UiText.Plain(throwable.message ?: searchFailedMessage),
                            )
                    }
            }
    }

    ReaderSheetTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = settings.appearance.monetEnabled,
        monetKeyColor = settings.appearance.monetKeyColor,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            DictionaryLookupPopupStack(
                lookups = lookupStack,
                settings = settings,
                ankiDuplicateExpression = duplicateExpression,
                isDark = isDark,
                materialEInkMode = materialEInkMode,
                viewportWidth = maxWidth,
                viewportHeight = maxHeight,
                topInset = topInset,
                bottomInset = bottomInset,
                blurEnabled = false,
                backdrop = null,
                firstPopupPlacement = DictionaryFirstPopupPlacement.TopFullWidth,
                onPopupTextSelected = { popupIndex, text, rect ->
                    appendLookup(
                        parentIndex = popupIndex,
                        text = text,
                        rect = rect,
                    )
                },
                onMineEntry = { lookup, content ->
                    coroutineScope.launch {
                        runCatching {
                            ankiService.mineNote(
                                settings = settings.anki,
                                content = content,
                                context = AnkiMiningContext(sentence = lookup.selectedText),
                            )
                        }.onSuccess {
                            onMessage("Added Anki note")
                        }.onFailure { throwable ->
                            onMessage(throwable.message ?: "Failed to add Anki note")
                        }
                    }
                },
                onCheckDuplicate = { expression ->
                    coroutineScope.launch {
                        runCatching { ankiService.checkDuplicate(settings.anki, expression) }
                            .onSuccess { isDuplicate ->
                                duplicateExpression = expression.takeIf { isDuplicate }
                            }
                    }
                },
                onSwipeDismiss = ::dismissLookup,
                onDismiss = ::dismissLookup,
                onOutsideClick = { dismissLookup(null) },
            )
        }
    }
}
