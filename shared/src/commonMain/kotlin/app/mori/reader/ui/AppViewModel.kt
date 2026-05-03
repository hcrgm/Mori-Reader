package app.mori.reader.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.anki.AnkiCardPayload
import app.mori.reader.data.anki.AnkiNoteType
import app.mori.reader.data.anki.AnkiRepository
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.anki.DuplicateScope
import app.mori.reader.data.anki.defaultAnkiFieldMappings
import app.mori.reader.data.audio.AudioRepository
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.book.BookRepository
import app.mori.reader.data.book.ReaderBook
import app.mori.reader.data.book.ReaderBookmark
import app.mori.reader.data.dictionary.DictionaryCatalog
import app.mori.reader.data.dictionary.DictionaryInfo
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.dictionary.DictionaryRepository
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.dictionary.MoveDirection
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.LanguageMode

import app.mori.reader.data.settings.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.ContactsBook
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit

enum class AppTab(
    val label: String,
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
) {
    Home(
        label = "书架",
        title = "书架",
        icon = MiuixIcons.Album,
    ),
    Dictionary(
        label = "词典",
        title = "词典",
        subtitle = "",
        icon = MiuixIcons.Translate,
    ),
    Settings(
        label = "设置",
        title = "设置",
        icon = MiuixIcons.Settings,
    ),
}

data class AppState(
    val currentTab: AppTab = AppTab.Home,
    val home: HomeState = HomeState(),
    val reader: ReaderState = ReaderState(),
    val dictionary: DictionaryState = DictionaryState(),
    val settings: SettingsState = SettingsState(),
    val anki: AnkiState = AnkiState(),
    val settingsLoaded: Boolean = false,
)

data class HomeState(
    val books: List<BookInfo> = emptyList(),
    val categories: List<BookCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedCategory: BookCategory?
        get() = categories.firstOrNull { it.id == selectedCategoryId }

    val visibleBooks: List<BookInfo>
        get() = selectedCategoryId?.let { categoryId ->
            books.filter { categoryId in it.categoryIds }
        } ?: books
}

data class ReaderState(
    val bookId: String? = null,
    val book: ReaderBook? = null,
    val chapterIndex: Int = 0,
    val chapterProgress: Double = 0.0,
    val fragment: String? = null,
    val navigationVersion: Int = 0,
    val verticalWriting: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lookupStack: List<ReaderLookupState> = emptyList(),
) {
    val currentChapter
        get() = book?.chapters?.getOrNull(chapterIndex)

    val currentCharacter: Int
        get() {
            val chapter = currentChapter ?: return 0
            return chapter.characterStart + (chapter.characterCount * chapterProgress.coerceIn(0.0, 1.0)).toInt()
        }

    val progressPercent: Double
        get() {
            val total = book?.totalCharacterCount ?: return 0.0
            if (total <= 0) return 0.0
            return currentCharacter.toDouble() / total.toDouble() * 100.0
        }
}

data class ReaderLookupState(
    val id: Int = 0,
    val selectedText: String = "",
    val sentence: String = "",
    val rect: ReaderSelectionRect? = null,
    val isSearching: Boolean = false,
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val dictionaryStyles: Map<String, String> = emptyMap(),
    val highlightLength: Int? = null,
    val errorMessage: String? = null,
) {
    val visible: Boolean
        get() = selectedText.isNotBlank() || isSearching || errorMessage != null
}

data class ReaderSelectionRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

private fun HomeState.withCatalog(
    books: List<BookInfo>,
    categories: List<BookCategory>,
    isLoading: Boolean = this.isLoading,
    isImporting: Boolean = this.isImporting,
    errorMessage: String? = this.errorMessage,
): HomeState {
    val categoryIds = categories.mapTo(mutableSetOf()) { it.id }
    val selectedCategoryId = this.selectedCategoryId.takeIf { it in categoryIds }
    return copy(
        books = books,
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        isLoading = isLoading,
        isImporting = isImporting,
        errorMessage = errorMessage,
    )
}

data class DictionaryState(
    val query: String = "",
    val lastQuery: String = "",
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val dictionaryStyles: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val popupStack: List<ReaderLookupState> = emptyList(),
)

data class SettingsState(
    val bookshelfSortMode: BookshelfSortMode = BookshelfSortMode.Recent,
    val themeMode: ThemeMode = ThemeMode.System,
    val languageMode: LanguageMode = LanguageMode.System,

    val readerThemeMode: ReaderThemeMode = ReaderThemeMode.FollowApp,
    val blurEnabled: Boolean = true,
    val maxResults: Int = 16,
    val scanLength: Int = 16,
    val readerFontSize: Int = 22,
    val readerLineHeight: Double = 1.65,
    val readerHorizontalPadding: Int = 5,
    val readerVerticalPadding: Int = 0,
    val readerAvoidPageBreak: Boolean = false,
    val readerJustifyText: Boolean = false,
    val readerLayoutAdvanced: Boolean = false,
    val readerCharacterSpacing: Double = 0.0,
    val readerContinuousMode: Boolean = false,
    val readerHideFurigana: Boolean = false,
    val readerFullscreen: Boolean = false,
    val popupWidth: Int = 320,
    val popupHeight: Int = 250,
    val popupFullWidth: Boolean = false,
    val popupSwipeToDismiss: Boolean = false,
    val popupSwipeThreshold: Int = 50,
    val collapseDictionaries: Boolean = false,
    val compactGlossaries: Boolean = true,
    val showExpressionTags: Boolean = false,
    val harmonicFrequency: Boolean = false,
    val deduplicatePitchAccents: Boolean = false,
    val audioSources: List<AudioSource> = listOf(AudioSource.Default),
    val enableLocalAudio: Boolean = true,
    val audioEnableAutoplay: Boolean = false,
    val audioPlaybackMode: AudioPlaybackMode = AudioPlaybackMode.Duck,
    val localAudioDatabaseSizeBytes: Long = 0L,
    val isImportingLocalAudio: Boolean = false,
    val anki: AnkiSettings = AnkiSettings(),
    val dictionaryManagement: DictionaryManagementState = DictionaryManagementState(),
)

data class AnkiState(
    val decks: List<String> = emptyList(),
    val noteTypes: List<AnkiNoteType> = emptyList(),
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val errorMessage: String? = null,
)

data class DictionaryManagementState(
    val selectedType: DictionaryType = DictionaryType.Term,
    val termDictionaries: List<DictionaryInfo> = emptyList(),
    val frequencyDictionaries: List<DictionaryInfo> = emptyList(),
    val pitchDictionaries: List<DictionaryInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val isUpdating: Boolean = false,
    val statusText: String = "",
    val errorMessage: String? = null,
) {
    fun dictionaries(type: DictionaryType = selectedType): List<DictionaryInfo> = when (type) {
        DictionaryType.Term -> termDictionaries
        DictionaryType.Frequency -> frequencyDictionaries
        DictionaryType.Pitch -> pitchDictionaries
    }

    val updatableCount: Int
        get() = termDictionaries.count { it.isUpdatable } +
            frequencyDictionaries.count { it.isUpdatable } +
            pitchDictionaries.count { it.isUpdatable }
}

sealed interface AppIntent {
    data class SelectTab(val tab: AppTab) : AppIntent
    data class ImportBooks(val uriStrings: List<String>) : AppIntent
    data class SelectBookCategory(val categoryId: String?) : AppIntent
    data class CreateBookCategory(val name: String) : AppIntent
    data class RenameBookCategory(val id: String, val name: String) : AppIntent
    data class ReorderBookCategories(val categoryIds: List<String>) : AppIntent
    data class DeleteBookCategory(val id: String) : AppIntent
    data class UpdateBookCategories(val bookId: String, val categoryIds: List<String>) : AppIntent
    data class DeleteBook(val id: String) : AppIntent
    data class OpenBook(val id: String) : AppIntent
    data class LoadReaderBook(val id: String) : AppIntent
    data class OpenReaderChapter(val index: Int, val fragment: String? = null) : AppIntent
    data object OpenReaderNextChapter : AppIntent
    data object OpenReaderPreviousChapter : AppIntent
    data class UpdateReaderProgress(val progress: Double) : AppIntent
    data class SaveReaderProgress(val progress: Double) : AppIntent
    data class ReaderTextSelected(
        val text: String,
        val sentence: String,
        val rect: ReaderSelectionRect?,
    ) : AppIntent
    data class ReaderPopupTextSelected(
        val parentIndex: Int,
        val text: String,
        val rect: ReaderSelectionRect?,
    ) : AppIntent
    data class JumpReaderToLink(val href: String) : AppIntent
    data class DismissReaderLookup(val index: Int? = null) : AppIntent
    data object ToggleReaderWritingMode : AppIntent
    data object DismissHomeError : AppIntent
    data class SetBookshelfSortMode(val mode: BookshelfSortMode) : AppIntent
    data class UpdateDictionaryQuery(val query: String) : AppIntent
    data object ExecuteDictionarySearch : AppIntent
    data object ClearDictionaryQuery : AppIntent
    data class DictionaryPopupTextSelected(
        val parentIndex: Int?,
        val text: String,
        val rect: ReaderSelectionRect?,
    ) : AppIntent
    data class DismissDictionaryPopup(val index: Int? = null) : AppIntent
    data class SetReaderFontSize(val value: Int) : AppIntent
    data class SetReaderLineHeight(val value: Double) : AppIntent
    data class SetReaderHorizontalPadding(val value: Int) : AppIntent
    data class SetReaderVerticalPadding(val value: Int) : AppIntent
    data class SetReaderAvoidPageBreak(val enabled: Boolean) : AppIntent
    data class SetReaderJustifyText(val enabled: Boolean) : AppIntent
    data class SetReaderLayoutAdvanced(val enabled: Boolean) : AppIntent
    data class SetReaderCharacterSpacing(val value: Double) : AppIntent
    data object ToggleReaderContinuousMode : AppIntent
    data object ToggleReaderHideFurigana : AppIntent
    data class SetReaderFullscreen(val enabled: Boolean) : AppIntent
    data class SetPopupWidth(val value: Int) : AppIntent
    data class SetPopupHeight(val value: Int) : AppIntent
    data object TogglePopupFullWidth : AppIntent
    data object TogglePopupSwipeToDismiss : AppIntent
    data class SetPopupSwipeThreshold(val value: Int) : AppIntent
    data object CycleThemeMode : AppIntent
    data class SetThemeMode(val mode: ThemeMode) : AppIntent
    data class SetLanguageMode(val mode: LanguageMode) : AppIntent

    data class SetReaderThemeMode(val mode: ReaderThemeMode) : AppIntent
    data class SetBlurEnabled(val enabled: Boolean) : AppIntent
    data class SelectDictionaryType(val type: DictionaryType) : AppIntent
    data class ImportDictionaries(val type: DictionaryType, val uriStrings: List<String>) : AppIntent
    data class SetDictionaryEnabled(val type: DictionaryType, val id: String, val enabled: Boolean) : AppIntent
    data class MoveDictionary(val type: DictionaryType, val id: String, val direction: MoveDirection) : AppIntent
    data class ReorderDictionaries(val type: DictionaryType, val ids: List<String>) : AppIntent
    data class DeleteDictionary(val type: DictionaryType, val id: String) : AppIntent
    data object UpdateDictionaries : AppIntent
    data object DismissDictionaryError : AppIntent
    data class SetMaxResults(val value: Int) : AppIntent
    data class SetScanLength(val value: Int) : AppIntent
    data class SetCollapseDictionaries(val enabled: Boolean) : AppIntent
    data class SetCompactGlossaries(val enabled: Boolean) : AppIntent
    data class SetShowExpressionTags(val enabled: Boolean) : AppIntent
    data class SetHarmonicFrequency(val enabled: Boolean) : AppIntent
    data class SetDeduplicatePitchAccents(val enabled: Boolean) : AppIntent
    data class SetAudioSourceEnabled(val url: String, val enabled: Boolean) : AppIntent
    data class AddAudioSource(val name: String, val url: String) : AppIntent
    data class UpdateAudioSource(val originalUrl: String, val name: String, val url: String) : AppIntent
    data class MoveAudioSource(val url: String, val direction: MoveDirection) : AppIntent
    data class ReorderAudioSources(val urls: List<String>) : AppIntent
    data class DeleteAudioSource(val url: String) : AppIntent
    data class SetEnableLocalAudio(val enabled: Boolean) : AppIntent
    data class SetAudioEnableAutoplay(val enabled: Boolean) : AppIntent
    data class SetAudioPlaybackMode(val mode: AudioPlaybackMode) : AppIntent
    data class ImportLocalAudioDatabase(val uriString: String) : AppIntent
    data object DeleteLocalAudioDatabase : AppIntent
    data class SetAnkiEnabled(val enabled: Boolean) : AppIntent
    data class SetAnkiEndpoint(val endpoint: String) : AppIntent
    data object TestAnkiConnection : AppIntent
    data object RefreshAnkiCatalog : AppIntent
    data class SelectAnkiDeck(val deck: String) : AppIntent
    data class SelectAnkiModel(val model: String) : AppIntent
    data class SetAnkiFieldTemplate(val fieldName: String, val template: String) : AppIntent
    data class InsertAnkiFieldToken(val fieldName: String, val token: String) : AppIntent
    data class SetAnkiAllowDuplicates(val enabled: Boolean) : AppIntent
    data class SetAnkiDuplicateScope(val scope: DuplicateScope) : AppIntent
    data class SetAnkiCheckAllModels(val enabled: Boolean) : AppIntent
    data class SetAnkiForceSync(val enabled: Boolean) : AppIntent
    data class SetAnkiTags(val tags: String) : AppIntent
    data class SetAnkiCompactGlossaries(val enabled: Boolean) : AppIntent
    data class AddAnkiCard(val payload: AnkiCardPayload) : AppIntent
    data object DismissAnkiError : AppIntent
}

sealed interface AppEffect {
    data class ShowMessage(val message: String) : AppEffect
    data class OpenReader(val bookId: String) : AppEffect
}

class AppViewModel(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val audioRepository: AudioRepository,
    private val ankiRepository: AnkiRepository,
    private val bookRepository: BookRepository,
    initialSettings: AppSettings? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AppState(
            settings = initialSettings?.toSettingsState() ?: SettingsState(),
            settingsLoaded = initialSettings != null,
        ),
    )
    val state = _state.asStateFlow()

    private val effectChannel = Channel<AppEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var dictionarySearchJob: Job? = null
    private var readerLookupNextId = 0

    init {
        viewModelScope.launch {
            runCatching { bookRepository.loadCatalog() }
                .onSuccess { catalog ->
                    _state.update { state ->
                        state.copy(
                            home = state.home.withCatalog(
                                books = catalog.books,
                                categories = catalog.categories,
                                isLoading = false,
                                errorMessage = null,
                            ),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            home = it.home.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "书架加载失败",
                            ),
                        )
                    }
                }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { state ->
                    state.copy(
                        settings = state.settings.copy(
                            bookshelfSortMode = settings.bookshelfSortMode,
                            themeMode = settings.themeMode,
                            languageMode = settings.languageMode,

                            readerThemeMode = settings.readerThemeMode,
                            blurEnabled = settings.blurEnabled,
                            maxResults = settings.maxResults,
                            scanLength = settings.scanLength,
                            readerFontSize = settings.readerFontSize,
                            readerLineHeight = settings.readerLineHeight,
                            readerHorizontalPadding = settings.readerHorizontalPadding,
                            readerVerticalPadding = settings.readerVerticalPadding,
                            readerAvoidPageBreak = settings.readerAvoidPageBreak,
                            readerJustifyText = settings.readerJustifyText,
                            readerLayoutAdvanced = settings.readerLayoutAdvanced,
                            readerCharacterSpacing = settings.readerCharacterSpacing,
                            readerContinuousMode = settings.readerContinuousMode,
                            readerHideFurigana = settings.readerHideFurigana,
                            readerFullscreen = settings.readerFullscreen,
                            popupWidth = settings.popupWidth,
                            popupHeight = settings.popupHeight,
                            popupFullWidth = settings.popupFullWidth,
                            popupSwipeToDismiss = settings.popupSwipeToDismiss,
                            popupSwipeThreshold = settings.popupSwipeThreshold,
                            collapseDictionaries = settings.collapseDictionaries,
                            compactGlossaries = settings.compactGlossaries,
                            showExpressionTags = settings.showExpressionTags,
                            harmonicFrequency = settings.harmonicFrequency,
                            deduplicatePitchAccents = settings.deduplicatePitchAccents,
                            audioSources = settings.audioSources,
                            enableLocalAudio = settings.enableLocalAudio,
                            audioEnableAutoplay = settings.audioEnableAutoplay,
                            audioPlaybackMode = settings.audioPlaybackMode,
                            localAudioDatabaseSizeBytes = settings.localAudioDatabaseSizeBytes,
                            anki = settings.anki,
                        ),
                        settingsLoaded = true,
                    )
                }
            }
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.loadDictionaries() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) { it.copy(isLoading = false) }
                }
                .onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "词典加载失败",
                        )
                    }
                }
        }
        viewModelScope.launch {
            val sizeBytes = audioRepository.localAudioDatabaseSizeBytes()
            settingsRepository.setLocalAudioDatabaseSizeBytes(sizeBytes)
        }
    }

    fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.SelectTab -> _state.update { it.copy(currentTab = intent.tab) }
            is AppIntent.ImportBooks -> importBooks(intent.uriStrings)
            is AppIntent.SelectBookCategory -> _state.update {
                it.copy(home = it.home.copy(selectedCategoryId = intent.categoryId))
            }

            is AppIntent.CreateBookCategory -> mutateBookCatalog(
                successMessage = "分类已创建",
            ) {
                bookRepository.createCategory(intent.name)
            }

            is AppIntent.RenameBookCategory -> mutateBookCatalog(
                successMessage = "分类已重命名",
            ) {
                bookRepository.renameCategory(intent.id, intent.name)
            }

            is AppIntent.ReorderBookCategories -> viewModelScope.launch {
                runCatching { bookRepository.reorderCategories(intent.categoryIds) }
                    .onSuccess { catalog ->
                        _state.update { state ->
                            state.copy(
                                home = state.home.withCatalog(
                                    books = catalog.books,
                                    categories = catalog.categories,
                                    errorMessage = null,
                                ),
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _state.update {
                            it.copy(home = it.home.copy(errorMessage = throwable.message ?: "分类排序失败"))
                        }
                    }
            }

            is AppIntent.DeleteBookCategory -> mutateBookCatalog(
                successMessage = "分类已删除",
            ) {
                bookRepository.deleteCategory(intent.id)
            }

            is AppIntent.UpdateBookCategories -> mutateBookCatalog(
                successMessage = "图书分类已更新",
            ) {
                bookRepository.updateBookCategories(intent.bookId, intent.categoryIds)
            }

            is AppIntent.DeleteBook -> mutateBookCatalog(
                successMessage = "图书已删除",
            ) {
                bookRepository.deleteBook(intent.id)
            }

            is AppIntent.OpenBook -> viewModelScope.launch {
                effectChannel.send(AppEffect.OpenReader(intent.id))
            }

            is AppIntent.LoadReaderBook -> loadReaderBook(intent.id)
            is AppIntent.OpenReaderChapter -> openReaderChapter(intent.index, intent.fragment)
            AppIntent.OpenReaderNextChapter -> openReaderAdjacentChapter(delta = 1)
            AppIntent.OpenReaderPreviousChapter -> openReaderAdjacentChapter(delta = -1)
            is AppIntent.UpdateReaderProgress -> updateReaderProgress(intent.progress, persist = false)
            is AppIntent.SaveReaderProgress -> updateReaderProgress(intent.progress, persist = true)
            is AppIntent.ReaderTextSelected -> lookupReaderSelection(intent.text, intent.sentence, intent.rect)
            is AppIntent.ReaderPopupTextSelected -> {
                lookupReaderSelection(
                    text = intent.text,
                    sentence = intent.text,
                    rect = intent.rect,
                    parentIndex = intent.parentIndex,
                )
            }
            is AppIntent.JumpReaderToLink -> jumpReaderToLink(intent.href)
            is AppIntent.DismissReaderLookup -> dismissReaderLookup(intent.index)

            AppIntent.ToggleReaderWritingMode -> _state.update {
                it.copy(
                    reader = it.reader.copy(
                        verticalWriting = !it.reader.verticalWriting,
                        navigationVersion = it.reader.navigationVersion + 1,
                    ),
                )
            }
            AppIntent.DismissHomeError -> _state.update {
                it.copy(home = it.home.copy(errorMessage = null))
            }

            is AppIntent.SetBookshelfSortMode -> {
                if (_state.value.settings.bookshelfSortMode == intent.mode) return
                _state.update { it.copy(settings = it.settings.copy(bookshelfSortMode = intent.mode)) }
                viewModelScope.launch {
                    settingsRepository.setBookshelfSortMode(intent.mode)
                }
            }

            is AppIntent.UpdateDictionaryQuery -> {
                if (_state.value.dictionary.query == intent.query) return
                _state.update {
                    it.copy(
                        dictionary = it.dictionary.copy(
                            query = intent.query,
                            popupStack = emptyList(),
                        ),
                    )
                }
                scheduleDictionarySearch()
            }

            AppIntent.ClearDictionaryQuery -> {
                if (_state.value.dictionary.query.isEmpty()) return
                dictionarySearchJob?.cancel()
                _state.update {
                    it.copy(
                        dictionary = it.dictionary.copy(
                            query = "",
                            lastQuery = "",
                            isSearching = false,
                            hasSearched = false,
                            entries = emptyList(),
                            dictionaryStyles = emptyMap(),
                            errorMessage = null,
                            popupStack = emptyList(),
                        ),
                    )
                }
            }

            AppIntent.ExecuteDictionarySearch -> runDictionarySearch(immediate = true)
            is AppIntent.DictionaryPopupTextSelected -> {
                lookupDictionaryPopup(
                    text = intent.text,
                    rect = intent.rect,
                    parentIndex = intent.parentIndex,
                )
            }
            is AppIntent.DismissDictionaryPopup -> dismissDictionaryPopup(intent.index)

            is AppIntent.SetReaderFontSize -> {
                val value = intent.value.coerceIn(16, 40)
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerFontSize = value))
                }
                viewModelScope.launch { settingsRepository.setReaderFontSize(value) }
            }

            is AppIntent.SetReaderLineHeight -> {
                val value = intent.value.coerceIn(1.0, 2.5)
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerLineHeight = value))
                }
                viewModelScope.launch { settingsRepository.setReaderLineHeight(value) }
            }

            is AppIntent.SetReaderHorizontalPadding -> {
                val value = intent.value.coerceIn(0, 50)
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerHorizontalPadding = value))
                }
                viewModelScope.launch { settingsRepository.setReaderHorizontalPadding(value) }
            }

            is AppIntent.SetReaderVerticalPadding -> {
                val value = intent.value.coerceIn(0, 50)
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerVerticalPadding = value))
                }
                viewModelScope.launch { settingsRepository.setReaderVerticalPadding(value) }
            }

            is AppIntent.SetReaderAvoidPageBreak -> {
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerAvoidPageBreak = intent.enabled))
                }
                viewModelScope.launch { settingsRepository.setReaderAvoidPageBreak(intent.enabled) }
            }

            is AppIntent.SetReaderJustifyText -> {
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerJustifyText = intent.enabled))
                }
                viewModelScope.launch { settingsRepository.setReaderJustifyText(intent.enabled) }
            }

            is AppIntent.SetReaderLayoutAdvanced -> {
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerLayoutAdvanced = intent.enabled))
                }
                viewModelScope.launch { settingsRepository.setReaderLayoutAdvanced(intent.enabled) }
            }

            is AppIntent.SetReaderCharacterSpacing -> {
                val value = intent.value.coerceIn(-10.0, 10.0)
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerCharacterSpacing = value))
                }
                viewModelScope.launch { settingsRepository.setReaderCharacterSpacing(value) }
            }

            AppIntent.ToggleReaderContinuousMode -> {
                val enabled = !_state.value.settings.readerContinuousMode
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerContinuousMode = enabled))
                }
                viewModelScope.launch { settingsRepository.setReaderContinuousMode(enabled) }
            }

            AppIntent.ToggleReaderHideFurigana -> {
                val enabled = !_state.value.settings.readerHideFurigana
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerHideFurigana = enabled))
                }
                viewModelScope.launch { settingsRepository.setReaderHideFurigana(enabled) }
            }

            is AppIntent.SetReaderFullscreen -> {
                if (_state.value.settings.readerFullscreen == intent.enabled) return
                _state.update { it.copy(settings = it.settings.copy(readerFullscreen = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setReaderFullscreen(intent.enabled) }
            }

            is AppIntent.SetPopupWidth -> {
                val value = intent.value.coerceIn(100, 700)
                _state.update { it.copy(settings = it.settings.copy(popupWidth = value)) }
                viewModelScope.launch { settingsRepository.setPopupWidth(value) }
            }

            is AppIntent.SetPopupHeight -> {
                val value = intent.value.coerceIn(100, 500)
                _state.update { it.copy(settings = it.settings.copy(popupHeight = value)) }
                viewModelScope.launch { settingsRepository.setPopupHeight(value) }
            }

            AppIntent.TogglePopupFullWidth -> {
                val enabled = !_state.value.settings.popupFullWidth
                _state.update { it.copy(settings = it.settings.copy(popupFullWidth = enabled)) }
                viewModelScope.launch { settingsRepository.setPopupFullWidth(enabled) }
            }

            AppIntent.TogglePopupSwipeToDismiss -> {
                val enabled = !_state.value.settings.popupSwipeToDismiss
                _state.update { it.copy(settings = it.settings.copy(popupSwipeToDismiss = enabled)) }
                viewModelScope.launch { settingsRepository.setPopupSwipeToDismiss(enabled) }
            }

            is AppIntent.SetPopupSwipeThreshold -> {
                val value = intent.value.coerceIn(20, 80)
                _state.update { it.copy(settings = it.settings.copy(popupSwipeThreshold = value)) }
                viewModelScope.launch { settingsRepository.setPopupSwipeThreshold(value) }
            }

            is AppIntent.SetLanguageMode -> {
                if (_state.value.settings.languageMode == intent.mode) return
                _state.update { it.copy(settings = it.settings.copy(languageMode = intent.mode)) }
                viewModelScope.launch {
                    settingsRepository.setLanguageMode(intent.mode)
                }
            }


            AppIntent.CycleThemeMode -> cycleThemeMode()
            is AppIntent.SetThemeMode -> {
                if (_state.value.settings.themeMode == intent.mode) return
                _state.update { it.copy(settings = it.settings.copy(themeMode = intent.mode)) }
                viewModelScope.launch {
                    settingsRepository.setThemeMode(intent.mode)
                }
            }

            is AppIntent.SetReaderThemeMode -> {
                if (_state.value.settings.readerThemeMode == intent.mode) return
                updateReaderLayout {
                    it.copy(settings = it.settings.copy(readerThemeMode = intent.mode))
                }
                viewModelScope.launch {
                    settingsRepository.setReaderThemeMode(intent.mode)
                }
            }

            is AppIntent.SetBlurEnabled -> {
                if (_state.value.settings.blurEnabled == intent.enabled) return
                _state.update { it.copy(settings = it.settings.copy(blurEnabled = intent.enabled)) }
                viewModelScope.launch {
                    settingsRepository.setBlurEnabled(intent.enabled)
                }
                sendEffect(if (intent.enabled) "模糊效果已开启" else "模糊效果已关闭")
            }

            is AppIntent.SelectDictionaryType -> updateDictionaryManagement {
                it.copy(selectedType = intent.type)
            }

            is AppIntent.ImportDictionaries -> importDictionaries(intent.type, intent.uriStrings)
            is AppIntent.SetDictionaryEnabled -> mutateDictionaries {
                dictionaryRepository.setEnabled(intent.type, intent.id, intent.enabled)
            }

            is AppIntent.MoveDictionary -> mutateDictionaries {
                dictionaryRepository.move(intent.type, intent.id, intent.direction)
            }

            is AppIntent.ReorderDictionaries -> mutateDictionaries {
                dictionaryRepository.reorder(intent.type, intent.ids)
            }

            is AppIntent.DeleteDictionary -> mutateDictionaries {
                dictionaryRepository.delete(intent.type, intent.id)
            }

            AppIntent.UpdateDictionaries -> updateDictionaries()
            AppIntent.DismissDictionaryError -> updateDictionaryManagement {
                it.copy(errorMessage = null)
            }

            is AppIntent.SetMaxResults -> {
                val value = intent.value.coerceIn(1, 50)
                _state.update { it.copy(settings = it.settings.copy(maxResults = value)) }
                viewModelScope.launch { settingsRepository.setMaxResults(value) }
                runDictionarySearch(immediate = true)
            }

            is AppIntent.SetScanLength -> {
                val value = intent.value.coerceIn(1, 64)
                _state.update { it.copy(settings = it.settings.copy(scanLength = value)) }
                viewModelScope.launch { settingsRepository.setScanLength(value) }
            }

            is AppIntent.SetCollapseDictionaries -> {
                _state.update { it.copy(settings = it.settings.copy(collapseDictionaries = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setCollapseDictionaries(intent.enabled) }
            }

            is AppIntent.SetCompactGlossaries -> {
                _state.update { it.copy(settings = it.settings.copy(compactGlossaries = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setCompactGlossaries(intent.enabled) }
            }

            is AppIntent.SetShowExpressionTags -> {
                _state.update { it.copy(settings = it.settings.copy(showExpressionTags = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setShowExpressionTags(intent.enabled) }
            }

            is AppIntent.SetHarmonicFrequency -> {
                _state.update { it.copy(settings = it.settings.copy(harmonicFrequency = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setHarmonicFrequency(intent.enabled) }
            }

            is AppIntent.SetDeduplicatePitchAccents -> {
                _state.update { it.copy(settings = it.settings.copy(deduplicatePitchAccents = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setDeduplicatePitchAccents(intent.enabled) }
            }

            is AppIntent.SetAudioSourceEnabled -> {
                if (intent.url == AudioSource.Local.url) {
                    _state.update { it.copy(settings = it.settings.copy(enableLocalAudio = intent.enabled)) }
                    viewModelScope.launch { settingsRepository.setEnableLocalAudio(intent.enabled) }
                } else {
                    updateAudioSources { sources ->
                        sources.map { if (it.url == intent.url) it.copy(isEnabled = intent.enabled) else it }
                    }
                }
            }

            is AppIntent.AddAudioSource -> addAudioSource(intent.name, intent.url)
            is AppIntent.UpdateAudioSource -> updateAudioSource(
                originalUrl = intent.originalUrl,
                name = intent.name,
                url = intent.url,
            )
            is AppIntent.MoveAudioSource -> updateAudioSources { sources ->
                val currentIndex = sources.indexOfFirst { it.url == intent.url }
                if (currentIndex == -1) return@updateAudioSources sources
                val targetIndex = when (intent.direction) {
                    MoveDirection.Up -> currentIndex - 1
                    MoveDirection.Down -> currentIndex + 1
                }
                if (targetIndex !in sources.indices) return@updateAudioSources sources
                sources.toMutableList().also {
                    val moved = it.removeAt(currentIndex)
                    it.add(targetIndex, moved)
                }
            }
            is AppIntent.ReorderAudioSources -> updateAudioSources { sources ->
                reorderAudioSources(sources, intent.urls)
            }

            is AppIntent.DeleteAudioSource -> updateAudioSources { sources ->
                sources.filterNot { it.url == intent.url && !it.isDefault && !it.isLocal }
            }

            is AppIntent.SetEnableLocalAudio -> {
                _state.update { it.copy(settings = it.settings.copy(enableLocalAudio = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setEnableLocalAudio(intent.enabled) }
            }

            is AppIntent.SetAudioEnableAutoplay -> {
                _state.update { it.copy(settings = it.settings.copy(audioEnableAutoplay = intent.enabled)) }
                viewModelScope.launch { settingsRepository.setAudioEnableAutoplay(intent.enabled) }
            }

            is AppIntent.SetAudioPlaybackMode -> {
                _state.update { it.copy(settings = it.settings.copy(audioPlaybackMode = intent.mode)) }
                viewModelScope.launch { settingsRepository.setAudioPlaybackMode(intent.mode) }
            }

            is AppIntent.ImportLocalAudioDatabase -> importLocalAudioDatabase(intent.uriString)
            AppIntent.DeleteLocalAudioDatabase -> deleteLocalAudioDatabase()
            is AppIntent.SetAnkiEnabled -> updateAnkiSettings { it.copy(enabled = intent.enabled) }
            is AppIntent.SetAnkiEndpoint -> updateAnkiSettings { it.copy(endpoint = intent.endpoint) }
            AppIntent.TestAnkiConnection -> testAnkiConnection()
            AppIntent.RefreshAnkiCatalog -> refreshAnkiCatalog()
            is AppIntent.SelectAnkiDeck -> updateAnkiSettings { it.copy(selectedDeck = intent.deck) }
            is AppIntent.SelectAnkiModel -> selectAnkiModel(intent.model)
            is AppIntent.SetAnkiFieldTemplate -> updateAnkiField(intent.fieldName, intent.template, append = false)
            is AppIntent.InsertAnkiFieldToken -> updateAnkiField(intent.fieldName, intent.token, append = true)
            is AppIntent.SetAnkiAllowDuplicates -> updateAnkiSettings { it.copy(allowDuplicates = intent.enabled) }
            is AppIntent.SetAnkiDuplicateScope -> updateAnkiSettings { it.copy(duplicateScope = intent.scope) }
            is AppIntent.SetAnkiCheckAllModels -> updateAnkiSettings { it.copy(checkAllModels = intent.enabled) }
            is AppIntent.SetAnkiForceSync -> updateAnkiSettings { it.copy(forceSync = intent.enabled) }
            is AppIntent.SetAnkiTags -> updateAnkiSettings { it.copy(tags = intent.tags) }
            is AppIntent.SetAnkiCompactGlossaries -> updateAnkiSettings { it.copy(compactGlossaries = intent.enabled) }
            is AppIntent.AddAnkiCard -> addAnkiCard(intent.payload)
            AppIntent.DismissAnkiError -> _state.update { it.copy(anki = it.anki.copy(errorMessage = null)) }
        }
    }

    private fun importBooks(uriStrings: List<String>) {
        if (uriStrings.isEmpty()) return
        _state.update {
            it.copy(home = it.home.copy(isImporting = true, errorMessage = null))
        }
        viewModelScope.launch {
            runCatching { bookRepository.importBooks(uriStrings) }
                .onSuccess { catalog ->
                    _state.update { state ->
                        state.copy(
                            home = state.home.withCatalog(
                                books = catalog.books,
                                categories = catalog.categories,
                                isImporting = false,
                                errorMessage = null,
                            ),
                        )
                    }
                    sendEffect("图书已导入")
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            home = it.home.copy(
                                isImporting = false,
                                errorMessage = throwable.message ?: "图书导入失败",
                            ),
                        )
                    }
                }
        }
    }

    private fun loadReaderBook(bookId: String) {
        val current = _state.value.reader
        if (current.bookId == bookId && current.book != null && !current.isLoading) return
        _state.update {
            it.copy(
                reader = it.reader.copy(
                    bookId = bookId,
                    isLoading = true,
                    errorMessage = null,
                    lookupStack = emptyList(),
                ),
            )
        }
        viewModelScope.launch {
            runCatching { bookRepository.loadReaderBook(bookId) }
                .onSuccess { book ->
                    val chapterIndex = book.bookmark.chapterIndex.coerceIn(book.chapters.indices)
                    val chapterProgress = book.bookmark.chapterProgress.coerceIn(0.0, 1.0)
                    _state.update { state ->
                        state.copy(
                            reader = state.reader.copy(
                                bookId = bookId,
                                book = book,
                                chapterIndex = chapterIndex,
                                chapterProgress = chapterProgress,
                                fragment = null,
                                navigationVersion = state.reader.navigationVersion + 1,
                                isLoading = false,
                                errorMessage = null,
                            ),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            reader = it.reader.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "阅读器加载失败",
                            ),
                        )
                    }
                }
        }
    }

    private fun openReaderChapter(index: Int, fragment: String?) {
        val reader = _state.value.reader
        val book = reader.book ?: return
        if (index !in book.chapters.indices) return
        _state.update {
            it.copy(
                reader = it.reader.withBookmark(
                    chapterIndex = index,
                    chapterProgress = 0.0,
                    fragment = fragment,
                    navigationVersion = it.reader.navigationVersion + 1,
                ).copy(lookupStack = emptyList()),
            )
        }
        persistReaderProgress(index, 0.0)
    }

    private fun jumpReaderToLink(href: String) {
        val reader = _state.value.reader
        val book = reader.book ?: return
        val currentChapter = reader.currentChapter ?: return
        val destination = resolveReaderLinkDestination(
            currentHref = currentChapter.href,
            href = href,
            book = book,
        ) ?: return

        if (destination.chapterIndex == reader.chapterIndex) {
            _state.update {
                it.copy(
                    reader = it.reader.withBookmark(
                        chapterIndex = reader.chapterIndex,
                        chapterProgress = if (destination.fragment == null) 0.0 else reader.chapterProgress,
                        fragment = destination.fragment,
                        navigationVersion = it.reader.navigationVersion + 1,
                    ).copy(lookupStack = emptyList()),
                )
            }
            if (destination.fragment == null) {
                persistReaderProgress(reader.chapterIndex, 0.0)
            }
            return
        }

        _state.update {
            it.copy(
                reader = it.reader.withBookmark(
                    chapterIndex = destination.chapterIndex,
                    chapterProgress = 0.0,
                    fragment = destination.fragment,
                    navigationVersion = it.reader.navigationVersion + 1,
                ).copy(lookupStack = emptyList()),
            )
        }
        persistReaderProgress(destination.chapterIndex, 0.0)
    }

    private fun openReaderAdjacentChapter(delta: Int) {
        val reader = _state.value.reader
        val book = reader.book ?: return
        val target = reader.chapterIndex + delta
        if (target !in book.chapters.indices) {
            sendEffect(if (delta > 0) "已经是最后一章" else "已经是第一章")
            return
        }
        val progress = if (delta > 0) 0.0 else 0.99
        _state.update {
            it.copy(
                reader = it.reader.withBookmark(
                    chapterIndex = target,
                    chapterProgress = progress,
                    fragment = null,
                    navigationVersion = it.reader.navigationVersion + 1,
                ).copy(lookupStack = emptyList()),
            )
        }
        persistReaderProgress(target, progress)
    }

    private fun updateReaderProgress(progress: Double, persist: Boolean) {
        val reader = _state.value.reader
        val clamped = progress.coerceIn(0.0, 1.0)
        _state.update {
            it.copy(
                reader = it.reader.withBookmark(
                    chapterIndex = reader.chapterIndex,
                    chapterProgress = clamped,
                    fragment = null,
                    navigationVersion = it.reader.navigationVersion,
                ),
            )
        }
        if (persist) {
            persistReaderProgress(reader.chapterIndex, clamped)
        }
    }

    private fun persistReaderProgress(chapterIndex: Int, progress: Double) {
        val bookId = _state.value.reader.bookId ?: return
        viewModelScope.launch {
            runCatching { bookRepository.saveReaderProgress(bookId, chapterIndex, progress) }
                .onSuccess { catalog ->
                    _state.update { state ->
                        state.copy(
                            home = state.home.withCatalog(
                                books = catalog.books,
                                categories = catalog.categories,
                                errorMessage = null,
                            ),
                        )
                    }
                }
        }
    }

    private fun lookupReaderSelection(
        text: String,
        sentence: String,
        rect: ReaderSelectionRect?,
        parentIndex: Int? = null,
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            dismissReaderLookup(parentIndex)
            return
        }
        val lookupId = ++readerLookupNextId
        _state.update {
            val baseStack = if (parentIndex == null) {
                emptyList()
            } else {
                it.reader.lookupStack.take(parentIndex + 1)
            }
            it.copy(
                reader = it.reader.copy(
                    lookupStack = baseStack + ReaderLookupState(
                        id = lookupId,
                        selectedText = trimmed,
                        sentence = sentence.trim(),
                        rect = rect,
                        isSearching = true,
                    ),
                ),
            )
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.lookup(trimmed, _state.value.settings.maxResults) }
                .onSuccess { result ->
                    _state.update {
                        val updatedStack = it.reader.lookupStack.map { lookup ->
                            if (lookup.id != lookupId) return@map lookup
                            lookup.copy(
                                isSearching = false,
                                entries = result.entries,
                                dictionaryStyles = result.styles,
                                highlightLength = result.entries.firstOrNull()?.matched?.codePointLength(),
                                errorMessage = null,
                            )
                        }
                        it.copy(
                            reader = it.reader.copy(
                                lookupStack = updatedStack,
                            ),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        val updatedStack = it.reader.lookupStack.map { lookup ->
                            if (lookup.id != lookupId) return@map lookup
                            lookup.copy(
                                isSearching = false,
                                highlightLength = null,
                                errorMessage = throwable.message ?: "查词失败",
                            )
                        }
                        it.copy(
                            reader = it.reader.copy(
                                lookupStack = updatedStack,
                            ),
                        )
                    }
                }
        }
    }

    private fun dismissReaderLookup(index: Int?) {
        _state.update { state ->
            val nextStack = when (index) {
                null -> emptyList()
                else -> state.reader.lookupStack.take(index)
            }
            state.copy(reader = state.reader.copy(lookupStack = nextStack))
        }
    }

    private fun lookupDictionaryPopup(
        text: String,
        rect: ReaderSelectionRect?,
        parentIndex: Int? = null,
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            dismissDictionaryPopup(parentIndex)
            return
        }
        val lookupId = ++readerLookupNextId
        _state.update {
            val baseStack = if (parentIndex == null) {
                emptyList()
            } else {
                it.dictionary.popupStack.take(parentIndex + 1)
            }
            it.copy(
                dictionary = it.dictionary.copy(
                    popupStack = baseStack + ReaderLookupState(
                        id = lookupId,
                        selectedText = trimmed,
                        sentence = trimmed,
                        rect = rect,
                        isSearching = true,
                    ),
                ),
            )
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.lookup(trimmed, _state.value.settings.maxResults) }
                .onSuccess { result ->
                    _state.update {
                        val updatedStack = it.dictionary.popupStack.map { lookup ->
                            if (lookup.id != lookupId) return@map lookup
                            lookup.copy(
                                isSearching = false,
                                entries = result.entries,
                                dictionaryStyles = result.styles,
                                highlightLength = result.entries.firstOrNull()?.matched?.codePointLength(),
                                errorMessage = null,
                            )
                        }
                        it.copy(
                            dictionary = it.dictionary.copy(
                                popupStack = updatedStack,
                            ),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        val updatedStack = it.dictionary.popupStack.map { lookup ->
                            if (lookup.id != lookupId) return@map lookup
                            lookup.copy(
                                isSearching = false,
                                highlightLength = null,
                                errorMessage = throwable.message ?: "查词失败",
                            )
                        }
                        it.copy(
                            dictionary = it.dictionary.copy(
                                popupStack = updatedStack,
                            ),
                        )
                    }
                }
        }
    }

    private fun dismissDictionaryPopup(index: Int?) {
        _state.update { state ->
            val nextStack = when (index) {
                null -> emptyList()
                else -> state.dictionary.popupStack.take(index)
            }
            state.copy(dictionary = state.dictionary.copy(popupStack = nextStack))
        }
    }

    private fun mutateBookCatalog(
        successMessage: String,
        block: suspend () -> app.mori.reader.data.book.BookCatalog,
    ) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { catalog ->
                    _state.update { state ->
                        state.copy(
                            home = state.home.withCatalog(
                                books = catalog.books,
                                categories = catalog.categories,
                                errorMessage = null,
                            ),
                        )
                    }
                    sendEffect(successMessage)
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(home = it.home.copy(errorMessage = throwable.message ?: "分类操作失败"))
                    }
                }
        }
    }

    private fun updateAnkiSettings(block: (AnkiSettings) -> AnkiSettings) {
        val updated = block(_state.value.settings.anki)
        _state.update { it.copy(settings = it.settings.copy(anki = updated)) }
        viewModelScope.launch { settingsRepository.setAnkiSettings(updated) }
    }

    private fun testAnkiConnection() {
        _state.update { it.copy(anki = it.anki.copy(isLoading = true, errorMessage = null)) }
        viewModelScope.launch {
            runCatching { ankiRepository.ping(_state.value.settings.anki.endpoint) }
                .onSuccess { ok ->
                    _state.update { it.copy(anki = it.anki.copy(isLoading = false)) }
                    sendEffect(if (ok) "AnkiDroid 连接成功" else "AnkiDroid 不可用")
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "AnkiDroid 连接失败"
                    _state.update {
                        it.copy(
                            anki = it.anki.copy(
                                isLoading = false,
                                errorMessage = message,
                            ),
                        )
                    }
                    sendEffect(message)
                }
        }
    }

    private fun refreshAnkiCatalog() {
        _state.update { it.copy(anki = it.anki.copy(isLoading = true, errorMessage = null)) }
        viewModelScope.launch {
            runCatching { ankiRepository.fetchDecksAndModels(_state.value.settings.anki.endpoint) }
                .onSuccess { catalog ->
                    val current = _state.value.settings.anki
                    val selectedDeck = current.selectedDeck.takeIf { it in catalog.decks } ?: catalog.decks.firstOrNull().orEmpty()
                    val selectedModel = current.selectedModel.takeIf { model -> catalog.noteTypes.any { it.name == model } }
                        ?: catalog.noteTypes.firstOrNull()?.name.orEmpty()
                    val mappings = ensureMappings(
                        settings = current.copy(selectedDeck = selectedDeck, selectedModel = selectedModel),
                        noteTypes = catalog.noteTypes,
                    )
                    val settings = current.copy(
                        selectedDeck = selectedDeck,
                        selectedModel = selectedModel,
                        fieldMappingsByModel = mappings,
                    )
                    _state.update {
                        it.copy(
                            settings = it.settings.copy(anki = settings),
                            anki = it.anki.copy(
                                decks = catalog.decks,
                                noteTypes = catalog.noteTypes,
                                isLoading = false,
                            ),
                        )
                    }
                    settingsRepository.setAnkiSettings(settings)
                    sendEffect("Anki 牌组和模板已刷新")
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Anki 配置拉取失败"
                    _state.update {
                        it.copy(
                            anki = it.anki.copy(
                                isLoading = false,
                                errorMessage = message,
                            ),
                        )
                    }
                }
        }
    }

    private fun selectAnkiModel(model: String) {
        updateAnkiSettings { settings ->
            settings.copy(
                selectedModel = model,
                fieldMappingsByModel = ensureMappings(
                    settings = settings.copy(selectedModel = model),
                    noteTypes = _state.value.anki.noteTypes,
                ),
            )
        }
    }

    private fun updateAnkiField(fieldName: String, value: String, append: Boolean) {
        updateAnkiSettings { settings ->
            val model = settings.selectedModel
            if (model.isBlank()) return@updateAnkiSettings settings
            val current = settings.selectedFieldMappings
            val updated = current.map { mapping ->
                if (mapping.fieldName == fieldName) {
                    val template = if (append) {
                        (mapping.template + value).trim()
                    } else {
                        value
                    }
                    mapping.copy(template = template)
                } else {
                    mapping
                }
            }
            settings.copy(fieldMappingsByModel = settings.fieldMappingsByModel + (model to updated))
        }
    }

    private fun addAnkiCard(payload: AnkiCardPayload) {
        val settings = _state.value.settings.anki
        if (!settings.enabled) {
            sendEffect("Anki 未启用")
            return
        }
        if (settings.selectedDeck.isBlank() || settings.selectedModel.isBlank()) {
            sendEffect("请先选择 Anki 牌组和模板")
            return
        }
        _state.update { it.copy(anki = it.anki.copy(isAdding = true, errorMessage = null)) }
        viewModelScope.launch {
            runCatching {
                val canAdd = ankiRepository.canAdd(settings, payload)
                if (!canAdd.canAdd) {
                    throw IllegalStateException(canAdd.message.ifBlank { "Anki 已存在重复卡片" })
                }
                ankiRepository.addNote(settings, payload)
            }.onSuccess {
                _state.update { it.copy(anki = it.anki.copy(isAdding = false)) }
                sendEffect("Anki 卡片已添加")
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        anki = it.anki.copy(
                            isAdding = false,
                            errorMessage = throwable.message ?: "Anki 加卡失败",
                        ),
                    )
                }
                sendEffect(throwable.message ?: "Anki 加卡失败")
            }
        }
    }

    private fun ensureMappings(
        settings: AnkiSettings,
        noteTypes: List<AnkiNoteType>,
    ): Map<String, List<app.mori.reader.data.anki.AnkiFieldMapping>> {
        val model = settings.selectedModel
        if (model.isBlank() || settings.fieldMappingsByModel[model].orEmpty().isNotEmpty()) {
            return settings.fieldMappingsByModel
        }
        val fields = noteTypes.firstOrNull { it.name == model }?.fields.orEmpty()
        return settings.fieldMappingsByModel + (model to defaultAnkiFieldMappings(fields))
    }

    private fun addAudioSource(name: String, url: String) {
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isBlank() || trimmedUrl.isBlank()) return
        if (!trimmedUrl.contains("{term}") && !trimmedUrl.contains("{reading}")) {
            sendEffect("音源 URL 需要包含 {term} 或 {reading}")
            return
        }
        updateAudioSources { sources ->
            if (sources.any { it.url == trimmedUrl }) sources else sources + AudioSource(trimmedName, trimmedUrl)
        }
    }

    private fun updateAudioSource(originalUrl: String, name: String, url: String) {
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isBlank() || trimmedUrl.isBlank()) return
        if (!trimmedUrl.contains("{term}") && !trimmedUrl.contains("{reading}")) {
            sendEffect("音源 URL 需要包含 {term} 或 {reading}")
            return
        }
        updateAudioSources { sources ->
            val currentIndex = sources.indexOfFirst { it.url == originalUrl }
            if (currentIndex == -1) return@updateAudioSources sources
            val current = sources[currentIndex]
            if (current.isLocal) return@updateAudioSources sources
            if (trimmedUrl != originalUrl && sources.any { it.url == trimmedUrl }) {
                sendEffect("音源 URL 已存在")
                return@updateAudioSources sources
            }

            sources.toMutableList().apply {
                this[currentIndex] = current.copy(
                    name = trimmedName,
                    url = trimmedUrl,
                )
            }
        }
    }

    private fun updateAudioSources(block: (List<AudioSource>) -> List<AudioSource>) {
        val updated = block(_state.value.settings.audioSources)
        _state.update { it.copy(settings = it.settings.copy(audioSources = updated)) }
        viewModelScope.launch {
            settingsRepository.setAudioSources(updated)
        }
    }

    private fun reorderAudioSources(
        sources: List<AudioSource>,
        urls: List<String>,
    ): List<AudioSource> {
        if (urls.size != sources.size) return sources
        val sourceByUrl = sources.associateBy(AudioSource::url)
        val reordered = urls.mapNotNull(sourceByUrl::get)
        return if (reordered.size == sources.size) reordered else sources
    }

    private fun importLocalAudioDatabase(uriString: String) {
        _state.update {
            it.copy(settings = it.settings.copy(isImportingLocalAudio = true))
        }
        viewModelScope.launch {
            runCatching { audioRepository.importLocalAudioDatabase(uriString) }
                .onSuccess { sizeBytes ->
                    _state.update {
                        val updatedSources = it.settings.audioSources.map { source ->
                            if (source.isLocal) {
                                source.copy(isEnabled = true)
                            } else {
                                source
                            }
                        }
                        it.copy(
                            settings = it.settings.copy(
                                audioSources = updatedSources,
                                localAudioDatabaseSizeBytes = sizeBytes,
                                enableLocalAudio = true,
                                isImportingLocalAudio = false,
                            ),
                        )
                    }
                    settingsRepository.setLocalAudioDatabaseSizeBytes(sizeBytes)
                    settingsRepository.setEnableLocalAudio(true)
                    sendEffect("本地音频已导入")
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(settings = it.settings.copy(isImportingLocalAudio = false))
                    }
                    sendEffect(throwable.message ?: "本地音频导入失败")
                }
        }
    }

    private fun deleteLocalAudioDatabase() {
        viewModelScope.launch {
            runCatching { audioRepository.deleteLocalAudioDatabase() }
                .onSuccess {
                    _state.update {
                        val updatedSources = it.settings.audioSources.map { source ->
                            if (source.isLocal) {
                                source.copy(isEnabled = false)
                            } else {
                                source
                            }
                        }
                        it.copy(
                            settings = it.settings.copy(
                                audioSources = updatedSources,
                                localAudioDatabaseSizeBytes = 0L,
                                enableLocalAudio = false,
                            ),
                        )
                    }
                    settingsRepository.setLocalAudioDatabaseSizeBytes(0L)
                    settingsRepository.setEnableLocalAudio(false)
                    sendEffect("本地音频已删除")
                }
                .onFailure { throwable ->
                    sendEffect(throwable.message ?: "本地音频删除失败")
                }
        }
    }

    private fun importDictionaries(type: DictionaryType, uriStrings: List<String>) {
        if (uriStrings.isEmpty()) return
        updateDictionaryManagement {
            it.copy(isImporting = true, statusText = "正在导入词典", errorMessage = null)
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.importDictionaries(type, uriStrings) }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) {
                        it.copy(isImporting = false, statusText = "")
                    }
                    runDictionarySearch(immediate = true)
                    sendEffect("词典已导入")
                }
                .onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isImporting = false,
                            statusText = "",
                            errorMessage = throwable.message ?: "词典导入失败",
                        )
                    }
                }
        }
    }

    private fun updateDictionaries() {
        updateDictionaryManagement {
            it.copy(isUpdating = true, statusText = "正在检查词典更新", errorMessage = null)
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.updateDictionaries() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) {
                        it.copy(isUpdating = false, statusText = "")
                    }
                    runDictionarySearch(immediate = true)
                    sendEffect("词典更新检查完成")
                }
                .onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isUpdating = false,
                            statusText = "",
                            errorMessage = throwable.message ?: "词典更新失败",
                        )
                    }
                }
        }
    }

    private fun mutateDictionaries(block: suspend () -> DictionaryCatalog) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) { it.copy(errorMessage = null) }
                    runDictionarySearch(immediate = true)
                }
                .onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(errorMessage = throwable.message ?: "词典操作失败")
                    }
                }
        }
    }

    private fun scheduleDictionarySearch() {
        dictionarySearchJob?.cancel()
        val query = _state.value.dictionary.query.trim()
        if (query.isEmpty()) {
            _state.update {
                it.copy(
                    dictionary = it.dictionary.copy(
                        lastQuery = "",
                        isSearching = false,
                        hasSearched = false,
                        entries = emptyList(),
                        dictionaryStyles = emptyMap(),
                        errorMessage = null,
                    ),
                )
            }
            return
        }
        dictionarySearchJob = viewModelScope.launch {
            delay(250)
            searchDictionary(query)
        }
    }

    private fun runDictionarySearch(immediate: Boolean) {
        dictionarySearchJob?.cancel()
        val query = _state.value.dictionary.query.trim()
        if (query.isEmpty()) {
            scheduleDictionarySearch()
            return
        }
        dictionarySearchJob = viewModelScope.launch {
            if (!immediate) delay(250)
            searchDictionary(query)
        }
    }

    private suspend fun searchDictionary(query: String) {
        val maxResults = _state.value.settings.maxResults
        _state.update {
            it.copy(
                dictionary = it.dictionary.copy(
                    lastQuery = query,
                    isSearching = true,
                    hasSearched = true,
                    errorMessage = null,
                ),
            )
        }
        runCatching { dictionaryRepository.lookup(query, maxResults) }
            .onSuccess { result ->
                _state.update {
                    if (it.dictionary.query.trim() != query) return@update it
                    it.copy(
                        dictionary = it.dictionary.copy(
                            isSearching = false,
                            entries = result.entries,
                            dictionaryStyles = result.styles,
                            errorMessage = null,
                        ),
                    )
                }
            }
            .onFailure { throwable ->
                _state.update {
                    if (it.dictionary.query.trim() != query) return@update it
                    it.copy(
                        dictionary = it.dictionary.copy(
                            isSearching = false,
                            entries = emptyList(),
                            dictionaryStyles = emptyMap(),
                            errorMessage = throwable.message ?: "查词失败",
                        ),
                    )
                }
            }
    }

    private fun updateDictionaryCatalog(
        catalog: DictionaryCatalog,
        transform: (DictionaryManagementState) -> DictionaryManagementState = { it },
    ) {
        updateDictionaryManagement {
            transform(
                it.copy(
                    termDictionaries = catalog.termDictionaries,
                    frequencyDictionaries = catalog.frequencyDictionaries,
                    pitchDictionaries = catalog.pitchDictionaries,
                ),
            )
        }
    }

    private fun updateDictionaryManagement(
        transform: (DictionaryManagementState) -> DictionaryManagementState,
    ) {
        _state.update { state ->
            state.copy(
                settings = state.settings.copy(
                    dictionaryManagement = transform(state.settings.dictionaryManagement),
                ),
            )
        }
    }

    private fun updateReaderLayout(
        transform: (AppState) -> AppState,
    ) {
        _state.update { state ->
            val updated = transform(state)
            if (updated.reader.book == null) {
                updated
            } else {
                updated.copy(
                    reader = updated.reader.copy(
                        navigationVersion = updated.reader.navigationVersion + 1,
                        fragment = null,
                    ),
                )
            }
        }
    }

    private fun cycleThemeMode() {
        val current = _state.value.settings.themeMode
        val next = when (current) {
            ThemeMode.System -> ThemeMode.Light
            ThemeMode.Light -> ThemeMode.Dark
            ThemeMode.Dark -> ThemeMode.System
        }
        onIntent(AppIntent.SetThemeMode(next))
    }

    private fun sendEffect(message: String) {
        effectChannel.trySend(AppEffect.ShowMessage(message))
    }
}

private fun ReaderState.withBookmark(
    chapterIndex: Int,
    chapterProgress: Double,
    fragment: String?,
    navigationVersion: Int,
): ReaderState {
    val currentBook = book ?: return copy(
        chapterIndex = chapterIndex,
        chapterProgress = chapterProgress,
        fragment = fragment,
        navigationVersion = navigationVersion,
    )
    val chapter = currentBook.chapters.getOrNull(chapterIndex)
    val characterCount = chapter?.let {
        it.characterStart + (it.characterCount * chapterProgress.coerceIn(0.0, 1.0)).toInt()
    } ?: 0
    val bookmark = ReaderBookmark(
        chapterIndex = chapterIndex,
        chapterProgress = chapterProgress.coerceIn(0.0, 1.0),
        characterCount = characterCount,
    )
    return copy(
        book = currentBook.copy(bookmark = bookmark),
        chapterIndex = chapterIndex,
        chapterProgress = chapterProgress.coerceIn(0.0, 1.0),
        fragment = fragment,
        navigationVersion = navigationVersion,
    )
}

private fun AppSettings.toSettingsState(): SettingsState =
    SettingsState(
        bookshelfSortMode = bookshelfSortMode,
        themeMode = themeMode,
        languageMode = languageMode,

        readerThemeMode = readerThemeMode,
        blurEnabled = blurEnabled,
        maxResults = maxResults,
        scanLength = scanLength,
        readerFontSize = readerFontSize,
        readerLineHeight = readerLineHeight,
        readerHorizontalPadding = readerHorizontalPadding,
        readerVerticalPadding = readerVerticalPadding,
        readerAvoidPageBreak = readerAvoidPageBreak,
        readerJustifyText = readerJustifyText,
        readerLayoutAdvanced = readerLayoutAdvanced,
        readerCharacterSpacing = readerCharacterSpacing,
        readerContinuousMode = readerContinuousMode,
        readerHideFurigana = readerHideFurigana,
        readerFullscreen = readerFullscreen,
        popupWidth = popupWidth,
        popupHeight = popupHeight,
        popupFullWidth = popupFullWidth,
        popupSwipeToDismiss = popupSwipeToDismiss,
        popupSwipeThreshold = popupSwipeThreshold,
        collapseDictionaries = collapseDictionaries,
        compactGlossaries = compactGlossaries,
        showExpressionTags = showExpressionTags,
        harmonicFrequency = harmonicFrequency,
        deduplicatePitchAccents = deduplicatePitchAccents,
        audioSources = audioSources,
        enableLocalAudio = enableLocalAudio,
        audioEnableAutoplay = audioEnableAutoplay,
        audioPlaybackMode = audioPlaybackMode,
        localAudioDatabaseSizeBytes = localAudioDatabaseSizeBytes,
        isImportingLocalAudio = false,
        anki = anki,
    )

private data class ReaderLinkDestination(
    val chapterIndex: Int,
    val fragment: String?,
)

private fun resolveReaderLinkDestination(
    currentHref: String,
    href: String,
    book: ReaderBook,
): ReaderLinkDestination? {
    val trimmed = href.trim()
    if (trimmed.isBlank()) return null

    val withoutQuery = trimmed.substringBefore('?')
    val rawPath = withoutQuery.substringBefore('#')
    val fragment = withoutQuery.substringAfter('#', "")
        .takeIf { it.isNotBlank() }
        ?.percentDecode()

    if (rawPath.isBlank()) {
        val currentIndex = book.chapters.firstOrNull { normalizeReaderPath(it.href) == normalizeReaderPath(currentHref) }?.index
            ?: return null
        return ReaderLinkDestination(currentIndex, fragment)
    }

    if (rawPath.hasUriScheme() && !rawPath.startsWith("file:", ignoreCase = true)) {
        return null
    }

    val destinationPath = if (rawPath.startsWith("file:", ignoreCase = true)) {
        rawPath.percentDecode().replace('\\', '/')
    } else {
        normalizeReaderPath(resolveReaderRelativePath(currentHref, rawPath))
    }

    val chapter = book.chapters.firstOrNull { chapter ->
        val chapterPath = normalizeReaderPath(chapter.href)
        if (rawPath.startsWith("file:", ignoreCase = true)) {
            destinationPath.endsWith("/$chapterPath") || destinationPath.endsWith(chapterPath)
        } else {
            chapterPath == destinationPath
        }
    } ?: return null

    return ReaderLinkDestination(chapter.index, fragment)
}

private fun resolveReaderRelativePath(currentHref: String, href: String): String {
    val decodedHref = href.percentDecode().replace('\\', '/')
    if (decodedHref.startsWith('/')) return decodedHref.trimStart('/')
    val baseDir = currentHref.substringBeforeLast('/', "")
    return if (baseDir.isBlank()) decodedHref else "$baseDir/$decodedHref"
}

private fun normalizeReaderPath(path: String): String {
    val normalized = mutableListOf<String>()
    path.percentDecode()
        .replace('\\', '/')
        .split('/')
        .filter { it.isNotBlank() && it != "." }
        .forEach { part ->
            if (part == "..") {
                if (normalized.isNotEmpty()) normalized.removeAt(normalized.lastIndex)
            } else {
                normalized += part
            }
        }
    return normalized.joinToString("/")
}

private fun String.hasUriScheme(): Boolean {
    val colon = indexOf(':')
    if (colon <= 0) return false
    return take(colon).withIndex().all { (index, char) ->
        if (index == 0) char.isLetter() else char.isLetterOrDigit() || char == '+' || char == '.' || char == '-'
    }
}

private fun String.percentDecode(): String {
    val result = StringBuilder(length)
    val bytes = mutableListOf<Byte>()

    fun flushBytes() {
        if (bytes.isNotEmpty()) {
            result.append(bytes.toByteArray().decodeToString())
            bytes.clear()
        }
    }

    var index = 0
    while (index < length) {
        val char = this[index]
        val high = getOrNull(index + 1)?.digitToIntOrNull(16)
        val low = getOrNull(index + 2)?.digitToIntOrNull(16)
        if (char == '%' && high != null && low != null) {
            bytes += ((high shl 4) + low).toByte()
            index += 3
        } else {
            flushBytes()
            result.append(char)
            index += 1
        }
    }
    flushBytes()
    return result.toString()
}

private fun String.codePointLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        val char = this[index]
        index += if (char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) {
            2
        } else {
            1
        }
        count++
    }
    return count
}

val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "跟随系统"
        ThemeMode.Light -> "浅色模式"
        ThemeMode.Dark -> "深色模式"
    }

val ReaderThemeMode.label: String
    get() = when (this) {
        ReaderThemeMode.FollowApp -> "跟随应用主题"
        ReaderThemeMode.Light -> "浅色模式"
        ReaderThemeMode.Dark -> "深色模式"
    }
