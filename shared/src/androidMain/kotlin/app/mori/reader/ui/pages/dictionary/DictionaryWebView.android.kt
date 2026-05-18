package app.mori.reader.ui.pages.dictionary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.dictionary.DictionaryFrequency
import app.mori.reader.data.dictionary.DictionaryFrequencyGroup
import app.mori.reader.data.dictionary.DictionaryGlossary
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.dictionary.DictionaryLookupResult
import app.mori.reader.data.dictionary.DictionaryPitchGroup
import app.mori.reader.data.dictionary.DictionaryTraceStep
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import de.manhhao.hoshi.HoshiDicts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal val WebJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

private const val DICTIONARY_WEB_VIEW_LOG_TAG = "MoriDictionaryWebView"

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
actual fun DictionaryWebView(
    state: DictionaryWebViewState,
    config: DictionaryWebViewSettings,
    modifier: Modifier,
    callbacks: DictionaryWebViewCallbacks,
) {
    val query = state.query
    val entries = state.entries
    val dictionaryStyles = state.dictionaryStyles
    val isSearching = state.isSearching
    val hasSearched = state.hasSearched
    val errorMessage = state.errorMessage
    val maxResults = config.maxResults
    val scanLength = config.scanLength
    val collapseDictionaries = config.collapseDictionaries
    val compactGlossaries = config.compactGlossaries
    val showExpressionTags = config.showExpressionTags
    val harmonicFrequency = config.harmonicFrequency
    val deduplicatePitchAccents = config.deduplicatePitchAccents
    val isDark = config.isDark
    val eInkMode = config.eInkMode
    val audioSources = config.audioSources
    val audioEnableAutoplay = config.audioEnableAutoplay
    val audioPlaybackMode = config.audioPlaybackMode
    val contentTopPadding = config.contentTopPadding
    val contentBottomPadding = config.contentBottomPadding
    val edgeToEdgeContent = config.edgeToEdgeContent
    val transparentBackground = config.transparentBackground
    val eInkEntryBorderEnabled = config.eInkEntryBorderEnabled
    val enableInternalPopup = config.enableInternalPopup
    val swipeDismissThreshold = config.swipeDismissThreshold
    val ankiNeedsAudio = config.ankiNeedsAudio
    val ankiAllowDuplicates = config.ankiAllowDuplicates
    val ankiUseAnkiConnect = config.ankiUseAnkiConnect
    val ankiEmbedMedia = config.ankiEmbedMedia
    val ankiCompactGlossaries = config.ankiCompactGlossaries
    val ankiDuplicateExpression = config.ankiDuplicateExpression
    val navigateBackToken = config.navigateBackToken
    val navigateForwardToken = config.navigateForwardToken
    val onVerticalScrollActiveChange = callbacks.onVerticalScrollActiveChange
    val onPopupTextSelected = callbacks.onPopupTextSelected
    val onMineEntry = callbacks.onMineEntry
    val onCheckDuplicate = callbacks.onCheckDuplicate
    val onSwipeDismiss = callbacks.onSwipeDismiss
    val onNavigationStateChange = callbacks.onNavigationStateChange
    val currentOnVerticalScrollActiveChange = rememberUpdatedState(onVerticalScrollActiveChange)
    val currentOnPopupTextSelected = rememberUpdatedState(onPopupTextSelected)
    val currentOnMineEntry = rememberUpdatedState(onMineEntry)
    val currentOnCheckDuplicate = rememberUpdatedState(onCheckDuplicate)
    val currentOnSwipeDismiss = rememberUpdatedState(onSwipeDismiss)
    val currentOnNavigationStateChange = rememberUpdatedState(onNavigationStateChange)
    val bridge = remember { DictionaryBridge() }
    val resourceHandler = remember { DictionaryWebResourceHandler(bridge.audioSourceResolver) }
    val htmlParams =
        remember(
            query,
            entries,
            dictionaryStyles,
            isSearching,
            hasSearched,
            errorMessage,
            state.searchingMessage,
            state.noResultsMessage,
            state.idleMessage,
            state.playPronunciationLabel,
            maxResults,
            scanLength,
            contentTopPadding,
            collapseDictionaries,
            compactGlossaries,
            showExpressionTags,
            harmonicFrequency,
            deduplicatePitchAccents,
            isDark,
            eInkMode,
            audioSources,
            audioEnableAutoplay,
            audioPlaybackMode,
            contentBottomPadding,
            edgeToEdgeContent,
            transparentBackground,
            eInkEntryBorderEnabled,
            enableInternalPopup,
            swipeDismissThreshold,
            ankiNeedsAudio,
            ankiAllowDuplicates,
            ankiUseAnkiConnect,
            ankiEmbedMedia,
            ankiCompactGlossaries,
            ankiDuplicateExpression,
        ) {
            DictionaryHtmlParams(
                query = query,
                entries = entries,
                styles = dictionaryStyles,
                isSearching = isSearching,
                hasSearched = hasSearched,
                errorMessage = errorMessage,
                searchingMessage = state.searchingMessage,
                noResultsMessage = state.noResultsMessage,
                idleMessage = state.idleMessage,
                playPronunciationLabel = state.playPronunciationLabel,
                maxResults = maxResults,
                scanLength = scanLength,
                topPadding = if (edgeToEdgeContent) contentTopPadding.value else contentTopPadding.value + 10f,
                collapseDictionaries = collapseDictionaries,
                compactGlossaries = compactGlossaries,
                showExpressionTags = showExpressionTags,
                harmonicFrequency = harmonicFrequency,
                deduplicatePitchAccents = deduplicatePitchAccents,
                isDark = isDark,
                eInkMode = eInkMode,
                audioSources = audioSources.filter { it.isEnabled }.map { it.url },
                audioEnableAutoplay = audioEnableAutoplay,
                audioPlaybackMode = audioPlaybackMode.wireName,
                bottomPadding = if (edgeToEdgeContent) contentBottomPadding.value else contentBottomPadding.value + 18f,
                edgeToEdgeContent = edgeToEdgeContent,
                transparentBackground = transparentBackground,
                eInkEntryBorderEnabled = eInkEntryBorderEnabled,
                enableInternalPopup = enableInternalPopup,
                swipeDismissThreshold = swipeDismissThreshold,
                ankiNeedsAudio = ankiNeedsAudio,
                ankiAllowDuplicates = ankiAllowDuplicates,
                ankiUseAnkiConnect = ankiUseAnkiConnect,
                ankiEmbedMedia = ankiEmbedMedia,
                ankiCompactGlossaries = ankiCompactGlossaries,
                ankiDuplicateExpression = ankiDuplicateExpression,
            )
        }
    val html by produceState(initialValue = "", htmlParams) {
        value = withContext(Dispatchers.Default) { dictionaryHtml(htmlParams) }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
                var downX = 0f
                var downY = 0f
                var directionLocked = false

                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                val dictionaryWebView = this
                addJavascriptInterface(
                    bridge.apply {
                        attach(dictionaryWebView)
                        this.onPopupTextSelected = { text, rect ->
                            currentOnPopupTextSelected.value(text, rect)
                        }
                        this.onMineEntry = { content -> currentOnMineEntry.value(content) }
                        this.onCheckDuplicate = { expression -> currentOnCheckDuplicate.value(expression) }
                        this.onSwipeDismiss = { currentOnSwipeDismiss.value() }
                        this.onNavigationStateChange = { back, forward ->
                            currentOnNavigationStateChange.value(back, forward)
                        }
                    },
                    "AndroidHoshi",
                )
                webViewClient =
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean = resourceHandler.shouldOverrideUrlLoading(view.context, request)

                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest,
                        ) = resourceHandler.shouldInterceptRequest(
                            request = request,
                            context = view.context,
                        )
                    }
                setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.x
                            downY = event.y
                            directionLocked = false
                            currentOnVerticalScrollActiveChange.value(false)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (!directionLocked) {
                                val deltaX = event.x - downX
                                val deltaY = event.y - downY
                                val absX = kotlin.math.abs(deltaX)
                                val absY = kotlin.math.abs(deltaY)
                                if (absX > touchSlop || absY > touchSlop) {
                                    directionLocked = true
                                    currentOnVerticalScrollActiveChange.value(absY > absX)
                                }
                            }
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL,
                        -> {
                            directionLocked = false
                            currentOnVerticalScrollActiveChange.value(false)
                        }
                    }
                    false
                }
                if (html.isNotEmpty()) {
                    tag = html
                    loadDataWithBaseURL("https://mori.dictionary/", html, "text/html", "UTF-8", null)
                }
            }
        },
        update = { webView ->
            bridge.attach(webView)
            bridge.onPopupTextSelected =
                { text, rect -> currentOnPopupTextSelected.value(text, rect) }
            bridge.onMineEntry = { content -> currentOnMineEntry.value(content) }
            bridge.onCheckDuplicate = { expression -> currentOnCheckDuplicate.value(expression) }
            bridge.onSwipeDismiss = { currentOnSwipeDismiss.value() }
            bridge.onNavigationStateChange = { back, forward ->
                currentOnNavigationStateChange.value(back, forward)
            }
            if (bridge.lastNavigateBackToken != navigateBackToken) {
                bridge.lastNavigateBackToken = navigateBackToken
                webView.evaluateJavascript("window.navigateBack?.()", null)
            }
            if (bridge.lastNavigateForwardToken != navigateForwardToken) {
                bridge.lastNavigateForwardToken = navigateForwardToken
                webView.evaluateJavascript("window.navigateForward?.()", null)
            }
            if (html.isNotEmpty() && webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(
                    "https://mori.dictionary/",
                    html,
                    "text/html",
                    "UTF-8",
                    null,
                )
            }
        },
    )
}

private class DictionaryBridge {
    private var webView: WebView? = null
    val audioSourceResolver = DictionaryAudioSourceResolver()
    private val player = AndroidWordAudioPlayer()
    var onPopupTextSelected: ((String, ReaderSelectionRect?) -> Unit)? = null
    var onMineEntry: ((AnkiMiningContent) -> Unit)? = null
    var onCheckDuplicate: ((String) -> Unit)? = null
    var onSwipeDismiss: (() -> Unit)? = null
    var onNavigationStateChange: ((Boolean, Boolean) -> Unit)? = null
    var lastNavigateBackToken: Int = 0
    var lastNavigateForwardToken: Int = 0

    fun attach(webView: WebView) {
        this.webView = webView
    }

    private val context: Context?
        get() = webView?.context?.applicationContext

    @JavascriptInterface
    fun lookup(
        text: String,
        maxResults: Int,
    ): String =
        runCatching {
            val entries =
                HoshiDicts
                    .lookup(
                        HoshiDicts.lookupObject,
                        text,
                        maxResults.coerceIn(1, 50),
                        scanLength = 16,
                    ).map { result ->
                        val term = result.term
                        DictionaryLookupEntry(
                            expression = term.expression,
                            reading = term.reading,
                            matched = result.matched,
                            deinflectionTrace =
                                result.process
                                    .reversed()
                                    .map { DictionaryTraceStep(name = it) },
                            glossaries =
                                term.glossaries.map {
                                    DictionaryGlossary(
                                        it.dictName,
                                        it.glossary,
                                        it.definitionTags,
                                        it.termTags,
                                    )
                                },
                            frequencies =
                                term.frequencies.map {
                                    DictionaryFrequencyGroup(
                                        dictionary = it.dictName,
                                        frequencies =
                                            it.frequencies.map { frequency ->
                                                DictionaryFrequency(
                                                    frequency.value,
                                                    frequency.displayValue,
                                                )
                                            },
                                    )
                                },
                            pitches =
                                term.pitches.map {
                                    DictionaryPitchGroup(
                                        it.dictName,
                                        it.pitchPositions.toList().distinct(),
                                    )
                                },
                            rules = term.rules.split(' ').filter { it.isNotBlank() },
                        )
                    }
            val styles =
                HoshiDicts.getStyles(HoshiDicts.lookupObject).associate { it.dictName to it.styles }
            WebJson.encodeToString(DictionaryLookupResult(entries, styles))
        }.getOrElse {
            """{"entries":[],"styles":{}}"""
        }

    @JavascriptInterface
    fun openChildPopup(payload: String) {
        val webView = webView ?: return
        val json =
            runCatching { WebJson.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return
        val text =
            json["text"]
                ?.jsonPrimitive
                ?.content
                ?.trim()
                .orEmpty()
        if (text.isBlank()) return
        val density = webView.resources.displayMetrics.density
        val location = IntArray(2)
        webView.getLocationInWindow(location)
        val rectJson = json["rect"]?.jsonObject
        val rect =
            rectJson?.let {
                ReaderSelectionRect(
                    x =
                        location[0] / density + (
                            it["x"]?.jsonPrimitive?.content?.toFloatOrNull()
                                ?: 0f
                        ),
                    y =
                        location[1] / density + (
                            it["y"]?.jsonPrimitive?.content?.toFloatOrNull()
                                ?: 0f
                        ),
                    width = it["width"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                    height = it["height"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                )
            }
        onPopupTextSelected?.invoke(text, rect)
    }

    @JavascriptInterface
    fun swipeDismiss() {
        onSwipeDismiss?.invoke()
    }

    @JavascriptInterface
    fun mineEntry(payload: String) {
        val content =
            runCatching { WebJson.decodeFromString<AnkiMiningContent>(payload) }
                .getOrElse { throwable ->
                    Log.e(
                        DICTIONARY_WEB_VIEW_LOG_TAG,
                        "Failed to decode Anki mining payload: ${payload.take(500)}",
                        throwable,
                    )
                    throw IllegalArgumentException("Invalid Anki mining payload", throwable)
                }
        onMineEntry?.invoke(content)
    }

    @JavascriptInterface
    fun checkDuplicate(expression: String) {
        val trimmed = expression.trim()
        if (trimmed.isNotBlank()) {
            onCheckDuplicate?.invoke(trimmed)
        }
    }

    @JavascriptInterface
    fun updateNavigationState(
        backCount: Int,
        forwardCount: Int,
    ) {
        onNavigationStateChange?.invoke(backCount > 0, forwardCount > 0)
    }

    @JavascriptInterface
    fun openLink(url: String) {
        DictionaryExternalLinkOpener.open(context, url)
    }

    @JavascriptInterface
    fun lookupRedirect(
        text: String,
        maxResults: Int,
    ): String = lookup(text, maxResults)

    @JavascriptInterface
    fun consumeAutoplay(key: String): Boolean = DictionaryAutoplayTracker.consume(key)

    @JavascriptInterface
    fun fetchAudioJson(sourceUrl: String): String {
        // Legacy sync bridge; prefer audio:// fetch so WebView does the work off the UI thread.
        return audioSourceResolver.resolveAudioSourceListJson(context, sourceUrl)
    }

    @JavascriptInterface
    fun fetchAudioAsync(
        requestId: String,
        sourceUrl: String,
    ) {
        val targetWebView = webView ?: return
        Thread {
            val payload = fetchAudioJson(sourceUrl)
            val script =
                buildString {
                    append("window.__moriResolveAudioFetch?.(")
                    append(WebJson.encodeToString(requestId))
                    append(", ")
                    append(WebJson.encodeToString(payload))
                    append(")")
                }
            targetWebView.post {
                if (webView === targetWebView) {
                    targetWebView.evaluateJavascript(script, null)
                }
            }
        }.start()
    }

    @JavascriptInterface
    fun playWordAudio(payload: String) {
        val appContext = context ?: return
        val jsonObject =
            runCatching { WebJson.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return
        val url = jsonObject["url"]?.jsonPrimitive?.content ?: return
        val mode =
            jsonObject["mode"]
                ?.jsonPrimitive
                ?.content
                ?.let { value -> AudioPlaybackMode.entries.firstOrNull { it.wireName == value } }
                ?: AudioPlaybackMode.Duck
        val playableUrl = audioSourceResolver.playableAudioUrl(appContext, url) ?: return
        player.play(appContext, playableUrl, mode)
    }
}
