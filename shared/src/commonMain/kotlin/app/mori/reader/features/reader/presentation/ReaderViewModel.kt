package app.mori.reader.features.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.audiobook.AudiobookPlayerRepository
import app.mori.reader.data.audiobook.AudiobookRepository
import app.mori.reader.data.audiobook.SasayakiMediaInfo
import app.mori.reader.data.book.BookRepository
import app.mori.reader.data.book.ReaderBook
import app.mori.reader.data.book.ReaderBookmark
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.effectiveReaderSettings
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.features.dictionary.domain.DictionaryLookupUseCase
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import app.mori.reader.features.lookup.presentation.createLookupStackEntry
import app.mori.reader.features.lookup.presentation.dismissLookupStack
import app.mori.reader.features.lookup.presentation.withLookupError
import app.mori.reader.features.lookup.presentation.withLookupResult
import app.mori.reader.features.settings.domain.shouldRefreshReaderLayout
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_reader_load_failed
import app.mori.reader.shared.generated.resources.error_search_failed
import app.mori.reader.ui.text.uiTextOr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val lookupText: DictionaryLookupUseCase,
    private val audiobookRepository: AudiobookRepository,
    private val audiobookPlayerRepository: AudiobookPlayerRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    private var readerLookupNextId = 0
    private var maxResults = 16
    private var autoPauseOnLookup = true
    private var autoScroll = true
    private var currentSettings: AppSettings? = null

    init {
        observePlayer()
        observeSettings()
        observeAudiobookAssets()
        loadReaderBook(bookId)
    }

    fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.LoadBook -> {
                if (intent.bookId != bookId) return
                loadReaderBook(intent.bookId)
            }

            is ReaderIntent.OpenChapter -> {
                openReaderChapter(intent.index, intent.fragment)
            }

            ReaderIntent.OpenNextChapter -> {
                openReaderAdjacentChapter(delta = 1)
            }

            ReaderIntent.OpenPreviousChapter -> {
                openReaderAdjacentChapter(delta = -1)
            }

            is ReaderIntent.JumpToCharacter -> {
                jumpReaderToCharacter(intent.characterCount)
            }

            ReaderIntent.CloseBook -> {
                closeBook()
            }

            ReaderIntent.TogglePlayback -> {
                toggleSasayakiPlayback()
            }

            ReaderIntent.PausePlayback -> {
                pauseSasayakiPlayback()
            }

            ReaderIntent.NextCue -> {
                viewModelScope.launch { audiobookPlayerRepository.nextCue() }
            }

            ReaderIntent.PreviousCue -> {
                viewModelScope.launch { audiobookPlayerRepository.previousCue() }
            }

            is ReaderIntent.SeekTo -> {
                viewModelScope.launch {
                    audiobookPlayerRepository.seekTo(intent.positionMs)
                }
            }

            is ReaderIntent.SetDelay -> {
                viewModelScope.launch {
                    audiobookPlayerRepository.setDelay(intent.delayMs)
                }
            }

            is ReaderIntent.SetRate -> {
                viewModelScope.launch {
                    audiobookPlayerRepository.setRate(intent.rate)
                }
            }

            is ReaderIntent.ReplayCue -> {
                playSasayakiCue(intent.cueId)
            }

            is ReaderIntent.ContinueFromCue -> {
                playSasayakiCue(intent.cueId)
            }

            is ReaderIntent.UpdateProgress -> {
                updateReaderProgress(
                    intent.progress,
                    persist = false,
                )
            }

            is ReaderIntent.SaveProgress -> {
                updateReaderProgress(intent.progress, persist = true)
            }

            is ReaderIntent.ToggleCurrentBookmark -> {
                toggleCurrentBookmark(intent.snippet)
            }

            is ReaderIntent.DeleteBookmark -> {
                deleteBookmark(intent.bookmarkId)
            }

            is ReaderIntent.SetBookReaderScheme -> {
                _state.update { state ->
                    val currentBook = state.book ?: return@update state
                    state.copy(
                        book =
                            currentBook.copy(
                                info =
                                    currentBook.info.copy(
                                        readerSchemeId = intent.schemeId,
                                        lastReaderSchemeId = intent.schemeId ?: currentBook.info.lastReaderSchemeId,
                                    ),
                            ),
                        navigationVersion = state.navigationVersion + 1,
                        fragment = null,
                    )
                }
            }

            is ReaderIntent.TextSelected -> {
                lookupReaderSelection(
                    intent.text,
                    intent.sentence,
                    intent.rect,
                )
            }

            is ReaderIntent.PopupTextSelected -> {
                lookupReaderSelection(
                    text = intent.text,
                    sentence = intent.text,
                    rect = intent.rect,
                    parentIndex = intent.parentIndex,
                )
            }

            is ReaderIntent.JumpToLink -> {
                jumpReaderToLink(intent.href)
            }

            is ReaderIntent.DismissLookup -> {
                dismissReaderLookup(intent.index)
            }
        }
    }

    private fun observePlayer() {
        viewModelScope.launch {
            audiobookPlayerRepository.snapshot.collect { snapshot ->
                _state.update { state ->
                    state.copy(sasayakiPlayer = snapshot)
                }
                handleSasayakiCueTransition(snapshot.currentCueId)
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                val previous = currentSettings
                currentSettings = settings
                maxResults = settings.dictionary.maxResults
                autoPauseOnLookup = settings.sasayaki.autoPauseOnLookup
                autoScroll = settings.sasayaki.autoScroll
                reconcileBookReaderSchemes(settings)
                _state.update { state ->
                    if (state.verticalWriting == settings.reader.verticalWriting) {
                        state
                    } else {
                        state.copy(
                            verticalWriting = settings.reader.verticalWriting,
                            navigationVersion = state.navigationVersion + 1,
                        )
                    }
                }
                if (
                    previous != null &&
                    _state.value.book != null &&
                    shouldRefreshReaderLayout(
                        previous = previous.effectiveReaderSettings(_state.value.book?.info?.readerSchemeId),
                        next = settings.effectiveReaderSettings(_state.value.book?.info?.readerSchemeId),
                        previousThemeMode = previous.appearance.readerThemeMode,
                        nextThemeMode = settings.appearance.readerThemeMode,
                    )
                ) {
                    _state.update {
                        it.copy(
                            navigationVersion = it.navigationVersion + 1,
                            fragment = null,
                        )
                    }
                }
            }
        }
    }

    private fun observeAudiobookAssets() {
        viewModelScope.launch {
            audiobookRepository.observeAssets(bookId).collect { bundle ->
                _state.update {
                    it.copy(
                        sasayakiAudioAssetInfo = bundle.audioAssetInfo,
                        sasayakiMatches = bundle.matchData?.matches.orEmpty(),
                    )
                }
            }
        }
    }

    private fun reconcileBookReaderSchemes(settings: AppSettings) {
        val currentBook = _state.value.book ?: return
        val normalizedBook = normalizeBookReaderSchemes(currentBook, settings)
        if (normalizedBook.info == currentBook.info) return
        _state.update { state ->
            state.copy(
                book = normalizedBook,
                navigationVersion =
                    if (currentBook.info.readerSchemeId != normalizedBook.info.readerSchemeId) {
                        state.navigationVersion + 1
                    } else {
                        state.navigationVersion
                    },
                fragment =
                    if (currentBook.info.readerSchemeId != normalizedBook.info.readerSchemeId) {
                        null
                    } else {
                        state.fragment
                    },
            )
        }
    }

    private fun normalizeBookReaderSchemes(
        book: ReaderBook,
        settings: AppSettings?,
    ): ReaderBook {
        val currentInfo = book.info
        val normalizedInfo =
            currentInfo.normalizeReaderSchemes(
                validSchemeIds = settings?.readerPersonalizedSchemes.orEmpty().map { it.id }.toSet(),
            )
        if (normalizedInfo == currentInfo) return book
        viewModelScope.launch {
            runCatching {
                bookRepository.repairBookReaderSchemes(
                    bookId = currentInfo.id,
                    readerSchemeId = normalizedInfo.readerSchemeId,
                    lastReaderSchemeId = normalizedInfo.lastReaderSchemeId,
                )
            }
        }
        return book.copy(info = normalizedInfo)
    }

    private fun loadReaderBook(bookId: String) {
        val current = _state.value
        if (current.bookId == bookId && current.book != null && !current.isLoading) return
        _state.update {
            it.copy(
                bookId = bookId,
                isLoading = true,
                errorMessage = null,
                lookupStack = emptyList(),
            )
        }
        viewModelScope.launch {
            runCatching { bookRepository.loadReaderBook(bookId) }
                .onSuccess { book ->
                    val normalizedBook = normalizeBookReaderSchemes(book, currentSettings)
                    val audiobookBundle =
                        runCatching { audiobookRepository.loadAssets(bookId) }.getOrNull()
                    val sasayakiMatches = audiobookBundle?.matchData?.matches.orEmpty()
                    val chapterIndex = normalizedBook.bookmark.chapterIndex.coerceIn(normalizedBook.chapters.indices)
                    val chapterProgress = normalizedBook.bookmark.chapterProgress.coerceIn(0.0, 1.0)
                    _state.update { state ->
                        state.copy(
                            bookId = bookId,
                            book = normalizedBook,
                            sasayakiAudioAssetInfo = audiobookBundle?.audioAssetInfo,
                            sasayakiMatches = sasayakiMatches,
                            chapterIndex = chapterIndex,
                            chapterProgress = chapterProgress,
                            fragment = null,
                            navigationVersion = state.navigationVersion + 1,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                    val audioAsset = audiobookBundle?.audioAssetInfo
                    if (audioAsset != null && sasayakiMatches.isNotEmpty()) {
                        runCatching {
                            audiobookPlayerRepository.prepare(
                                bookId = bookId,
                                audioAssetInfo = audioAsset,
                                matches = sasayakiMatches,
                                mediaInfo = normalizedBook.sasayakiMediaInfo(),
                            )
                        }
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            sasayakiAudioAssetInfo = null,
                            sasayakiMatches = emptyList(),
                            isLoading = false,
                            errorMessage = throwable.uiTextOr(Res.string.error_reader_load_failed),
                        )
                    }
                }
        }
    }

    private fun openReaderChapter(
        index: Int,
        fragment: String?,
    ) {
        val reader = _state.value
        val book = reader.book ?: return
        if (index !in book.chapters.indices) return
        _state.update {
            it
                .withBookmark(
                    chapterIndex = index,
                    chapterProgress = 0.0,
                    fragment = fragment,
                    navigationVersion = it.navigationVersion + 1,
                ).copy(lookupStack = emptyList())
        }
        persistReaderProgress(index, 0.0)
    }

    private fun jumpReaderToLink(href: String) {
        val reader = _state.value
        val book = reader.book ?: return
        val currentChapter = reader.currentChapter ?: return
        val destination =
            resolveReaderLinkDestination(
                currentHref = currentChapter.href,
                href = href,
                book = book,
            ) ?: return

        if (destination.chapterIndex == reader.chapterIndex) {
            _state.update {
                it
                    .withBookmark(
                        chapterIndex = reader.chapterIndex,
                        chapterProgress = if (destination.fragment == null) 0.0 else reader.chapterProgress,
                        fragment = destination.fragment,
                        navigationVersion = it.navigationVersion + 1,
                    ).copy(lookupStack = emptyList())
            }
            if (destination.fragment == null) {
                persistReaderProgress(reader.chapterIndex, 0.0)
            }
            return
        }

        _state.update {
            it
                .withBookmark(
                    chapterIndex = destination.chapterIndex,
                    chapterProgress = 0.0,
                    fragment = destination.fragment,
                    navigationVersion = it.navigationVersion + 1,
                ).copy(lookupStack = emptyList())
        }
        persistReaderProgress(destination.chapterIndex, 0.0)
    }

    private fun jumpReaderToCharacter(characterCount: Int) {
        val reader = _state.value
        val book = reader.book ?: return
        if (book.chapters.isEmpty()) return

        val lastCharacter = (book.totalCharacterCount - 1).coerceAtLeast(0)
        val targetCharacter =
            if (book.totalCharacterCount > 0) {
                characterCount.coerceIn(0, lastCharacter)
            } else {
                0
            }

        val targetChapter =
            book.chapters.lastOrNull { chapter ->
                targetCharacter >= chapter.characterStart
            } ?: book.chapters.first()
        val offsetInChapter = (targetCharacter - targetChapter.characterStart).coerceAtLeast(0)
        val progress =
            if (targetChapter.characterCount <= 0) {
                0.0
            } else {
                ((offsetInChapter.toDouble() + 0.000001) / targetChapter.characterCount.toDouble())
                    .coerceIn(0.0, 0.999999)
            }

        _state.update {
            it
                .withBookmark(
                    chapterIndex = targetChapter.index,
                    chapterProgress = progress,
                    fragment = null,
                    navigationVersion = it.navigationVersion + 1,
                ).copy(lookupStack = emptyList())
        }
        persistReaderProgress(targetChapter.index, progress)
    }

    private fun openReaderAdjacentChapter(delta: Int) {
        val reader = _state.value
        val book = reader.book ?: return
        val target = reader.chapterIndex + delta
        if (target !in book.chapters.indices) return
        val progress = if (delta > 0) 0.0 else 0.99
        _state.update {
            it
                .withBookmark(
                    chapterIndex = target,
                    chapterProgress = progress,
                    fragment = null,
                    navigationVersion = it.navigationVersion + 1,
                ).copy(lookupStack = emptyList())
        }
        persistReaderProgress(target, progress)
    }

    private fun updateReaderProgress(
        progress: Double,
        persist: Boolean,
    ) {
        val reader = _state.value
        val clamped = progress.coerceIn(0.0, 1.0)
        _state.update {
            it.withBookmark(
                chapterIndex = reader.chapterIndex,
                chapterProgress = clamped,
                fragment = null,
                navigationVersion = it.navigationVersion,
            )
        }
        if (persist) {
            persistReaderProgress(reader.chapterIndex, clamped)
        }
    }

    private fun persistReaderProgress(
        chapterIndex: Int,
        progress: Double,
    ) {
        val reader = _state.value
        val currentBookId = reader.bookId ?: return
        val book = reader.book ?: return
        val chapter = book.chapters.getOrNull(chapterIndex) ?: return
        val clampedProgress = progress.coerceIn(0.0, 1.0)
        val bookmark =
            ReaderBookmark(
                chapterIndex = chapterIndex,
                chapterProgress = clampedProgress,
                characterCount =
                    (
                        chapter.characterStart +
                            (chapter.characterCount * clampedProgress).toInt()
                    ).coerceIn(0, book.totalCharacterCount),
            )
        viewModelScope.launch {
            runCatching {
                bookRepository.saveReaderProgress(currentBookId, bookmark)
            }
        }
    }

    private fun toggleSasayakiPlayback() {
        val reader = _state.value
        val bookId = reader.bookId ?: return
        viewModelScope.launch {
            if (!reader.sasayakiPlayer.isReady) {
                val bundle = runCatching { audiobookRepository.loadAssets(bookId) }.getOrNull()
                val audio = bundle?.audioAssetInfo
                val matches = bundle?.matchData?.matches.orEmpty()
                if (audio == null || matches.isEmpty()) {
                    return@launch
                }
                runCatching {
                    audiobookPlayerRepository.prepare(
                        bookId = bookId,
                        audioAssetInfo = audio,
                        matches = matches,
                        mediaInfo = reader.book?.sasayakiMediaInfo() ?: SasayakiMediaInfo(),
                    )
                }.onFailure {
                    return@launch
                }
            }
            runCatching { audiobookPlayerRepository.togglePlayPause() }
        }
    }

    private fun pauseSasayakiPlayback() {
        viewModelScope.launch {
            runCatching { audiobookPlayerRepository.pause() }
        }
    }

    private fun closeBook() {
        _state.update {
            it.copy(
                lookupStack = emptyList(),
                resumeSasayakiAfterLookup = false,
            )
        }
        viewModelScope.launch {
            runCatching { audiobookPlayerRepository.stop(bookId) }
        }
    }

    private fun playSasayakiCue(cueId: String) {
        viewModelScope.launch {
            runCatching {
                audiobookPlayerRepository.seekToCue(cueId)
                audiobookPlayerRepository.play()
            }
        }
    }

    private fun handleSasayakiCueTransition(cueId: String?) {
        cueId ?: return
        val reader = _state.value
        if (!autoScroll) return
        val cue = reader.sasayakiMatches.firstOrNull { it.id == cueId } ?: return
        if (cue.chapterIndex == reader.chapterIndex) return
        val book = reader.book ?: return
        if (cue.chapterIndex !in book.chapters.indices) return
        _state.update {
            it.withBookmark(
                chapterIndex = cue.chapterIndex,
                chapterProgress = 0.0,
                fragment = null,
                navigationVersion = it.navigationVersion + 1,
            )
        }
        persistReaderProgress(cue.chapterIndex, 0.0)
    }

    private fun sasayakiCueIdAtNormalizedOffset(
        reader: ReaderState,
        offset: Int?,
    ): String? {
        val normalizedOffset = offset ?: return null
        return reader.sasayakiMatches
            .firstOrNull { match ->
                match.chapterIndex == reader.chapterIndex &&
                    normalizedOffset >= match.start &&
                    normalizedOffset < match.start + match.length
            }?.id
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
        val currentReader = _state.value
        val shouldPauseSasayaki =
            parentIndex == null &&
                autoPauseOnLookup &&
                currentReader.sasayakiPlayer.isPlaying
        if (shouldPauseSasayaki) {
            pauseSasayakiPlayback()
        }
        val sasayakiCueId = sasayakiCueIdAtNormalizedOffset(currentReader, rect?.normalizedOffset)
        val lookupId = ++readerLookupNextId
        _state.update {
            it.copy(
                lookupStack =
                    createLookupStackEntry(
                        stack = it.lookupStack,
                        parentIndex = parentIndex,
                        lookupId = lookupId,
                        text = trimmed,
                        sentence = sentence.trim(),
                        rect = rect,
                        sasayakiCueId = sasayakiCueId,
                    ),
                resumeSasayakiAfterLookup = it.resumeSasayakiAfterLookup || shouldPauseSasayaki,
            )
        }
        viewModelScope.launch {
            runCatching { lookupText(trimmed, maxResults) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            lookupStack =
                                it.lookupStack.withLookupResult(
                                    lookupId = lookupId,
                                    entries = result.entries,
                                    dictionaryStyles = result.styles,
                                ),
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            lookupStack =
                                it.lookupStack.withLookupError(
                                    lookupId = lookupId,
                                    errorMessage = throwable.uiTextOr(Res.string.error_search_failed),
                                ),
                        )
                    }
                }
        }
    }

    private fun dismissReaderLookup(index: Int?) {
        var shouldResume = false
        _state.update { state ->
            val nextStack = dismissLookupStack(state.lookupStack, index)
            shouldResume = nextStack.isEmpty() && state.resumeSasayakiAfterLookup
            state.copy(
                lookupStack = nextStack,
                resumeSasayakiAfterLookup = if (nextStack.isEmpty()) false else state.resumeSasayakiAfterLookup,
            )
        }
        if (shouldResume) {
            viewModelScope.launch {
                runCatching { audiobookPlayerRepository.play() }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            runCatching { audiobookPlayerRepository.stop(bookId) }
        }
        super.onCleared()
    }

    private fun toggleCurrentBookmark(snippet: String) {
        val reader = _state.value
        val currentBookId = reader.bookId ?: return
        val book = reader.book ?: return
        val chapter = book.chapters.getOrNull(reader.chapterIndex) ?: return
        val existing = reader.currentSavedBookmark
        val nextBookmarks =
            if (existing != null) {
                book.savedBookmarks.filterNot { it.id == existing.id }
            } else {
                val createdAt = (book.savedBookmarks.maxOfOrNull(ReaderSavedBookmark::createdAt) ?: 0L) + 1L
                listOf(
                    ReaderSavedBookmark(
                        id = "${chapter.index}-${reader.currentCharacter}-$createdAt",
                        chapterIndex = reader.chapterIndex,
                        chapterProgress = reader.chapterProgress.coerceIn(0.0, 1.0),
                        characterCount = reader.currentCharacter,
                        snippet = snippet.asBookmarkSnippet(),
                        createdAt = createdAt,
                    ),
                ) + book.savedBookmarks
            }
        updateSavedBookmarks(
            bookId = currentBookId,
            bookmarks = nextBookmarks,
        )
    }

    private fun deleteBookmark(bookmarkId: String) {
        val reader = _state.value
        val currentBookId = reader.bookId ?: return
        val book = reader.book ?: return
        val nextBookmarks = book.savedBookmarks.filterNot { it.id == bookmarkId }
        if (nextBookmarks.size == book.savedBookmarks.size) return
        updateSavedBookmarks(
            bookId = currentBookId,
            bookmarks = nextBookmarks,
        )
    }

    private fun updateSavedBookmarks(
        bookId: String,
        bookmarks: List<ReaderSavedBookmark>,
    ) {
        val normalized =
            bookmarks
                .distinctBy(ReaderSavedBookmark::id)
                .sortedByDescending(ReaderSavedBookmark::createdAt)
        _state.update { state ->
            val currentBook = state.book ?: return@update state
            state.copy(book = currentBook.copy(savedBookmarks = normalized))
        }
        viewModelScope.launch {
            runCatching {
                bookRepository.saveReaderBookmarks(bookId, normalized)
            }
        }
    }
}

private fun app.mori.reader.data.book.BookInfo.normalizeReaderSchemes(validSchemeIds: Set<String>) =
    copy(
        readerSchemeId = readerSchemeId?.takeIf(validSchemeIds::contains),
        lastReaderSchemeId =
            when {
                lastReaderSchemeId != null && lastReaderSchemeId in validSchemeIds -> lastReaderSchemeId
                readerSchemeId != null && readerSchemeId in validSchemeIds -> readerSchemeId
                else -> null
            },
    )

private const val BOOKMARK_SNIPPET_MAX_LENGTH = 100

private fun String.asBookmarkSnippet(): String =
    replace(Regex("\\s+"), " ")
        .trim()
        .take(BOOKMARK_SNIPPET_MAX_LENGTH)

private fun ReaderBook.sasayakiMediaInfo(): SasayakiMediaInfo =
    SasayakiMediaInfo(
        title = info.title,
        coverPath = info.coverPath,
    )

private fun ReaderState.withBookmark(
    chapterIndex: Int,
    chapterProgress: Double,
    fragment: String?,
    navigationVersion: Int,
): ReaderState {
    val currentBook =
        book ?: return copy(
            chapterIndex = chapterIndex,
            chapterProgress = chapterProgress,
            fragment = fragment,
            navigationVersion = navigationVersion,
        )
    val chapter = currentBook.chapters.getOrNull(chapterIndex)
    val characterCount =
        chapter?.let {
            it.characterStart + (it.characterCount * chapterProgress.coerceIn(0.0, 1.0)).toInt()
        } ?: 0
    val bookmark =
        ReaderBookmark(
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
    val fragment =
        withoutQuery
            .substringAfter('#', "")
            .takeIf { it.isNotBlank() }
            ?.percentDecode()

    if (rawPath.isBlank()) {
        val currentIndex =
            book.chapters
                .firstOrNull {
                    normalizeReaderPath(it.href) == normalizeReaderPath(currentHref)
                }?.index
                ?: return null
        return ReaderLinkDestination(currentIndex, fragment)
    }

    if (rawPath.hasUriScheme() && !rawPath.startsWith("file:", ignoreCase = true)) {
        return null
    }

    val destinationPath =
        if (rawPath.startsWith("file:", ignoreCase = true)) {
            rawPath.percentDecode().replace('\\', '/')
        } else {
            normalizeReaderPath(resolveReaderRelativePath(currentHref, rawPath))
        }

    val chapter =
        book.chapters.firstOrNull { chapter ->
            val chapterPath = normalizeReaderPath(chapter.href)
            if (rawPath.startsWith("file:", ignoreCase = true)) {
                destinationPath.endsWith("/$chapterPath") || destinationPath.endsWith(chapterPath)
            } else {
                chapterPath == destinationPath
            }
        } ?: return null

    return ReaderLinkDestination(chapter.index, fragment)
}

private fun resolveReaderRelativePath(
    currentHref: String,
    href: String,
): String {
    val decodedHref = href.percentDecode().replace('\\', '/')
    if (decodedHref.startsWith('/')) return decodedHref.trimStart('/')
    val baseDir = currentHref.substringBeforeLast('/', "")
    return if (baseDir.isBlank()) decodedHref else "$baseDir/$decodedHref"
}

private fun normalizeReaderPath(path: String): String {
    val normalized = mutableListOf<String>()
    path
        .percentDecode()
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
