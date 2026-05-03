package app.mori.reader.ui.pages.dictionary

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import app.mori.reader.data.anki.AnkiCardPayload
import app.mori.reader.data.audio.AndroidLocalAudioStore
import app.mori.reader.data.dictionary.DictionaryFrequency
import app.mori.reader.data.dictionary.DictionaryFrequencyGroup
import app.mori.reader.data.dictionary.DictionaryGlossary
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.dictionary.DictionaryLookupResult
import app.mori.reader.data.dictionary.DictionaryPitchGroup
import app.mori.reader.data.dictionary.DictionaryTraceStep
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.ui.ReaderSelectionRect
import de.manhhao.hoshi.HoshiDicts
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val WebJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private const val AudioFetchConnectTimeoutMillis = 15_000
private const val AudioFetchReadTimeoutMillis = 45_000
private const val DictionaryAudioLogTag = "MoriDictionaryAudio"

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
actual fun DictionaryWebView(
    query: String,
    entries: List<DictionaryLookupEntry>,
    dictionaryStyles: Map<String, String>,
    isSearching: Boolean,
    hasSearched: Boolean,
    errorMessage: String?,
    maxResults: Int,
    scanLength: Int,
    collapseDictionaries: Boolean,
    compactGlossaries: Boolean,
    showExpressionTags: Boolean,
    harmonicFrequency: Boolean,
    deduplicatePitchAccents: Boolean,
    isDark: Boolean,
    audioSources: List<AudioSource>,
    audioEnableAutoplay: Boolean,
    audioPlaybackMode: AudioPlaybackMode,
    ankiEnabled: Boolean,
    modifier: Modifier,
    contentTopPadding: Dp,
    contentBottomPadding: Dp,
    edgeToEdgeContent: Boolean,
    transparentBackground: Boolean,
    enableInternalPopup: Boolean,
    swipeDismissThreshold: Int,
    onVerticalScrollActiveChange: (Boolean) -> Unit,
    onPopupTextSelected: (String, ReaderSelectionRect?) -> Unit,
    onSwipeDismiss: () -> Unit,
    onAddAnkiCard: (AnkiCardPayload) -> Unit,
) {
    val currentOnVerticalScrollActiveChange = rememberUpdatedState(onVerticalScrollActiveChange)
    val currentOnPopupTextSelected = rememberUpdatedState(onPopupTextSelected)
    val currentOnSwipeDismiss = rememberUpdatedState(onSwipeDismiss)
    val currentOnAddAnkiCard = rememberUpdatedState(onAddAnkiCard)
    val bridge = remember { DictionaryBridge() }
    val html = remember(
        query,
        entries,
        dictionaryStyles,
        isSearching,
        hasSearched,
        errorMessage,
        maxResults,
        scanLength,
        contentTopPadding,
        collapseDictionaries,
        compactGlossaries,
        showExpressionTags,
        harmonicFrequency,
        deduplicatePitchAccents,
        isDark,
        audioSources,
        audioEnableAutoplay,
        audioPlaybackMode,
        ankiEnabled,
        contentBottomPadding,
        edgeToEdgeContent,
        transparentBackground,
        enableInternalPopup,
        swipeDismissThreshold,
    ) {
        dictionaryHtml(
            query = query,
            entries = entries,
            styles = dictionaryStyles,
            isSearching = isSearching,
            hasSearched = hasSearched,
            errorMessage = errorMessage,
            maxResults = maxResults,
            scanLength = scanLength,
            topPadding = if (edgeToEdgeContent) contentTopPadding.value else contentTopPadding.value + 10f,
            collapseDictionaries = collapseDictionaries,
            compactGlossaries = compactGlossaries,
            showExpressionTags = showExpressionTags,
            harmonicFrequency = harmonicFrequency,
            deduplicatePitchAccents = deduplicatePitchAccents,
            isDark = isDark,
            audioSources = audioSources.filter { it.isEnabled }.map { it.url },
            audioEnableAutoplay = audioEnableAutoplay,
            audioPlaybackMode = audioPlaybackMode.wireName,
            ankiEnabled = ankiEnabled,
            bottomPadding = if (edgeToEdgeContent) contentBottomPadding.value else contentBottomPadding.value + 18f,
            edgeToEdgeContent = edgeToEdgeContent,
            transparentBackground = transparentBackground,
            enableInternalPopup = enableInternalPopup,
            swipeDismissThreshold = swipeDismissThreshold,
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
                var downX = 0f
                var downY = 0f
                var directionLocked = false

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                val dictionaryWebView = this
                addJavascriptInterface(
                    bridge.apply {
                        attach(dictionaryWebView)
                        this.onAddAnkiCard = { currentOnAddAnkiCard.value(it) }
                        this.onPopupTextSelected = { text, rect ->
                            currentOnPopupTextSelected.value(text, rect)
                        }
                        this.onSwipeDismiss = { currentOnSwipeDismiss.value() }
                    },
                    "AndroidHoshi",
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url?.toString().orEmpty()
                        return if (url.startsWith("http://") || url.startsWith("https://")) {
                            bridge.openLink(url)
                            true
                        } else {
                            false
                        }
                    }

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? {
                        val uri = request.url ?: return null
                        return when (uri.scheme) {
                            "image" -> {
                                val dictionary = uri.getQueryParameter("dictionary") ?: return null
                                val path = uri.getQueryParameter("path") ?: return null
                                val bytes = HoshiDicts.getMediaFile(
                                    HoshiDicts.lookupObject,
                                    dictionary,
                                    path
                                )
                                    ?: return null
                                WebResourceResponse(
                                    mimeTypeFor(path),
                                    null,
                                    ByteArrayInputStream(bytes),
                                )
                            }

                            "audio" -> bridge.fetchAudio(uri.getQueryParameter("url").orEmpty())
                            else -> null
                        }
                    }
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
                tag = html
                loadDataWithBaseURL("https://mori.dictionary/", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            bridge.attach(webView)
            bridge.onAddAnkiCard = { currentOnAddAnkiCard.value(it) }
            bridge.onPopupTextSelected = { text, rect -> currentOnPopupTextSelected.value(text, rect) }
            bridge.onSwipeDismiss = { currentOnSwipeDismiss.value() }
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(
                    "https://mori.dictionary/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
    )
}

private class DictionaryBridge {
    private var webView: WebView? = null
    private var cachedUserAgent: String? = null
    private val player = AndroidWordAudioPlayer()
    var onAddAnkiCard: ((AnkiCardPayload) -> Unit)? = null
    var onPopupTextSelected: ((String, ReaderSelectionRect?) -> Unit)? = null
    var onSwipeDismiss: (() -> Unit)? = null

    fun attach(webView: WebView) {
        this.webView = webView
        cachedUserAgent = webView.settings.userAgentString
    }

    private val context: Context?
        get() = webView?.context?.applicationContext

    @JavascriptInterface
    fun lookup(text: String, maxResults: Int): String =
        runCatching {
            val entries =
                HoshiDicts.lookup(HoshiDicts.lookupObject, text, maxResults.coerceIn(1, 50), scanLength = 16)
                    .map { result ->
                        val term = result.term
                        DictionaryLookupEntry(
                            expression = term.expression,
                            reading = term.reading,
                            matched = result.matched,
                            deinflectionTrace = result.process.reversed()
                                .map { DictionaryTraceStep(name = it) },
                            glossaries = term.glossaries.map {
                                DictionaryGlossary(
                                    it.dictName,
                                    it.glossary,
                                    it.definitionTags,
                                    it.termTags
                                )
                            },
                            frequencies = term.frequencies.map {
                                DictionaryFrequencyGroup(
                                    dictionary = it.dictName,
                                    frequencies = it.frequencies.map { frequency ->
                                        DictionaryFrequency(frequency.value, frequency.displayValue)
                                    },
                                )
                            },
                            pitches = term.pitches.map {
                                DictionaryPitchGroup(
                                    it.dictName,
                                    it.pitchPositions.toList().distinct()
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
        val json = runCatching { WebJson.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return
        val text = json["text"]?.jsonPrimitive?.content?.trim().orEmpty()
        if (text.isBlank()) return
        val density = webView.resources.displayMetrics.density
        val location = IntArray(2)
        webView.getLocationInWindow(location)
        val rectJson = json["rect"]?.jsonObject
        val rect = rectJson?.let {
            ReaderSelectionRect(
                x = location[0] / density + (it["x"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f),
                y = location[1] / density + (it["y"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f),
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
    fun openLink(url: String) {
        val appContext = context ?: return
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    @JavascriptInterface
    fun consumeAutoplay(key: String): Boolean =
        DictionaryAutoplayTracker.consume(key)

    fun fetchAudio(sourceUrl: String): WebResourceResponse {
        val body = resolveAudioSourceListJson(sourceUrl)
        return WebResourceResponse(
            "application/json",
            "UTF-8",
            ByteArrayInputStream(body.toByteArray()),
        )
    }

    @JavascriptInterface
    fun fetchAudioJson(sourceUrl: String): String {
        // Legacy sync bridge; prefer audio:// fetch so WebView does the work off the UI thread.
        return resolveAudioSourceListJson(sourceUrl)
    }

    @JavascriptInterface
    fun fetchAudioAsync(requestId: String, sourceUrl: String) {
        val targetWebView = webView ?: return
        Thread {
            val payload = resolveAudioSourceListJson(sourceUrl)
            val script = buildString {
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

    private fun resolveAudioSourceListJson(sourceUrl: String): String {
        val appContext = context
        return if (appContext != null && sourceUrl.startsWith("local://")) {
            runCatching {
                AndroidLocalAudioStore.audioSourceListJson(appContext, sourceUrl)
            }.onFailure {
                Log.e(
                    DictionaryAudioLogTag,
                    "resolveAudioSourceListJson local failed url=${sourceUrl.take(200)}",
                    it,
                )
            }.getOrDefault("""{"type":"audioSourceList","audioSources":[]}""")
        } else {
            runCatching {
                val connection = URL(sourceUrl).openConnection().apply {
                    connectTimeout = AudioFetchConnectTimeoutMillis
                    readTimeout = AudioFetchReadTimeoutMillis
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty(
                        "User-Agent",
                        cachedUserAgent ?: "Mozilla/5.0 (Android) Mori/1.0",
                    )
                }
                if (connection is HttpURLConnection) {
                    connection.instanceFollowRedirects = true
                    connection.requestMethod = "GET"
                    connection.doInput = true
                }
                try {
                    connection.getInputStream().bufferedReader().use { it.readText() }
                } finally {
                    if (connection is HttpURLConnection) {
                        connection.disconnect()
                    }
                }
            }
                .onFailure {
                    Log.e(
                        DictionaryAudioLogTag,
                        "resolveAudioSourceListJson remote failed url=${sourceUrl.take(200)}",
                        it,
                    )
                }
                .getOrDefault("""{"type":"audioSourceList","audioSources":[]}""")
        }
    }

    @JavascriptInterface
    fun playWordAudio(payload: String) {
        val appContext = context ?: return
        val jsonObject =
            runCatching { WebJson.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return
        val url = jsonObject["url"]?.jsonPrimitive?.content ?: return
        val mode = jsonObject["mode"]?.jsonPrimitive?.content
            ?.let { value -> AudioPlaybackMode.entries.firstOrNull { it.wireName == value } }
            ?: AudioPlaybackMode.Duck
        val playableUrl = if (url.startsWith("local://audio-file")) {
            val bytes = AndroidLocalAudioStore.audioBytes(appContext, url) ?: return
            val target = File(appContext.cacheDir, "word-audio-${System.nanoTime()}.mp3")
            target.writeBytes(bytes)
            target.toURI().toString()
        } else {
            url
        }
        player.play(appContext, playableUrl, mode)
    }

    @JavascriptInterface
    fun addAnkiCard(payload: String) {
        val card = runCatching {
            WebJson.decodeFromString(AnkiCardPayload.serializer(), payload)
        }.getOrNull() ?: return
        onAddAnkiCard?.invoke(card)
    }
}

private object DictionaryAutoplayTracker {
    private val consumedKeys = linkedSetOf<String>()

    fun consume(key: String): Boolean =
        synchronized(consumedKeys) {
            if (key.isBlank() || key in consumedKeys) {
                false
            } else {
                consumedKeys += key
                if (consumedKeys.size > 128) {
                    consumedKeys.remove(consumedKeys.first())
                }
                true
            }
        }
}

private fun dictionaryHtml(
    query: String,
    entries: List<DictionaryLookupEntry>,
    styles: Map<String, String>,
    isSearching: Boolean,
    hasSearched: Boolean,
    errorMessage: String?,
    maxResults: Int,
    scanLength: Int,
    topPadding: Float,
    collapseDictionaries: Boolean,
    compactGlossaries: Boolean,
    showExpressionTags: Boolean,
    harmonicFrequency: Boolean,
    deduplicatePitchAccents: Boolean,
    isDark: Boolean,
    audioSources: List<String>,
    audioEnableAutoplay: Boolean,
    audioPlaybackMode: String,
    ankiEnabled: Boolean,
    bottomPadding: Float,
    edgeToEdgeContent: Boolean,
    transparentBackground: Boolean,
    enableInternalPopup: Boolean,
    swipeDismissThreshold: Int,
): String {
    val entriesJson = WebJson.encodeToString(entries)
    val stylesJson = WebJson.encodeToString(styles)
    val audioSourcesJson = WebJson.encodeToString(audioSources)
    val message = when {
        errorMessage != null -> errorMessage
        isSearching -> "正在搜索..."
        hasSearched && entries.isEmpty() -> "没有找到词条"
        !hasSearched -> "输入词语开始搜索"
        else -> ""
    }
    return """
        <!doctype html>
        <html>
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>${dictionaryCss(topPadding, bottomPadding, edgeToEdgeContent, transparentBackground)}</style>
        </head>
        <body>
        <div id="entries-container"></div>
        <div id="empty-state"></div>
        <div id="popup-backdrop" hidden></div>
        <div id="popup" hidden><div id="popup-content"></div></div>
        <script>
        window.lookupEntries = $entriesJson;
        window.lookupQuery = ${WebJson.encodeToString(query)};
        window.dictionaryStyles = $stylesJson;
        window.emptyMessage = ${WebJson.encodeToString(message)};
        window.scanLength = $scanLength;
        window.maxResults = $maxResults;
        window.collapseDictionaries = $collapseDictionaries;
        window.compactGlossaries = $compactGlossaries;
        window.showExpressionTags = $showExpressionTags;
        window.harmonicFrequency = $harmonicFrequency;
        window.deduplicatePitchAccents = $deduplicatePitchAccents;
        window.isDark = ${WebJson.encodeToString(isDark)};
        window.audioSources = $audioSourcesJson;
        window.audioEnableAutoplay = $audioEnableAutoplay;
        window.audioPlaybackMode = ${WebJson.encodeToString(audioPlaybackMode)};
        window.ankiEnabled = $ankiEnabled;
        window.enableInternalPopup = $enableInternalPopup;
        window.swipeThreshold = ${swipeDismissThreshold.coerceIn(0, 80)};
        document.documentElement.classList.add(window.isDark ? 'dark' : 'light');
        </script>
        <script>${dictionaryJs()}</script>
        </body>
        </html>
    """.trimIndent()
}

private fun dictionaryCss(
    topPadding: Float,
    bottomPadding: Float,
    edgeToEdgeContent: Boolean,
    transparentBackground: Boolean,
): String {
    val containerPadding = if (edgeToEdgeContent) "0" else "0 2px"
    val pageBackground = if (transparentBackground || !edgeToEdgeContent) {
        "background: transparent;"
    } else {
        "background: var(--mori-entry-bg);"
    }
    val entryBackground = if (edgeToEdgeContent || transparentBackground) "transparent" else "var(--mori-entry-bg)"
    val entryRadius = if (edgeToEdgeContent) "0" else "8px"
    val lastEntryMargin = if (edgeToEdgeContent) """
    #entries-container > .entry:last-child { margin-bottom: 0; }
    #entries-container > hr:last-child { display: none; }
    """.trimIndent() else ""
    return """
    * { box-sizing: border-box; }
    html, body { margin: 0; min-height: 100%; $pageBackground color: var(--mori-text); font-family: sans-serif; -webkit-text-size-adjust: 100%; overscroll-behavior-y: contain; }
    :root {
        --mori-text: #101318;
        --mori-text-secondary: rgba(120, 126, 136, 0.95);
        --mori-entry-bg: rgba(255, 255, 255, 0.76);
        --mori-audio-button-bg: rgba(74,132,220,0.16);
        --mori-audio-button-color: #326bf5;
        --mori-tag-bg: rgba(120, 140, 160, 0.20);
        --mori-pitch-dict-bg: rgba(74, 132, 220, 0.72);
        --mori-pitch-dict-text: #fff;
        --mori-freq-border: rgba(74, 132, 220, 0.65);
        --mori-freq-dict-bg: rgba(74, 132, 220, 0.9);
        --mori-freq-dict-text: #fff;
        --mori-summary-color: rgba(82, 92, 110, 0.95);
        --mori-hr-border: rgba(128, 128, 128, 0.25);
        --mori-link-color: #326bf5;
        --mori-popup-bg: rgba(255,255,255,0.96);
        --mori-popup-shadow: 0 12px 34px rgba(0,0,0,0.24);
        --mori-selection-bg: rgba(255, 213, 84, 0.52);
        --mori-image-overlay: rgba(128,128,128,0.8);
    }
    :root.dark {
        --mori-text: #f4f6fb;
        --mori-text-secondary: rgba(190, 196, 210, 0.95);
        --mori-entry-bg: rgba(28, 31, 36, 0.78);
        --mori-audio-button-bg: rgba(108,158,240,0.24);
        --mori-audio-button-color: #8fb6ff;
        --mori-tag-bg: rgba(140, 152, 172, 0.22);
        --mori-pitch-dict-bg: rgba(74, 132, 220, 0.65);
        --mori-pitch-dict-text: #fff;
        --mori-freq-border: rgba(108, 158, 240, 0.55);
        --mori-freq-dict-bg: rgba(74, 132, 220, 0.8);
        --mori-freq-dict-text: #fff;
        --mori-summary-color: rgba(190, 196, 210, 0.95);
        --mori-hr-border: rgba(160, 160, 160, 0.22);
        --mori-link-color: #8fb6ff;
        --mori-popup-bg: rgba(31,34,40,0.98);
        --mori-popup-shadow: 0 12px 34px rgba(0,0,0,0.48);
        --mori-selection-bg: rgba(255, 213, 84, 0.52);
        --mori-image-overlay: rgba(160,160,160,0.8);
    }
    @media (prefers-color-scheme: dark) {
        :root:not(.dark):not(.light) {
            --mori-text: #f4f6fb;
            --mori-text-secondary: rgba(190, 196, 210, 0.95);
            --mori-entry-bg: rgba(28, 31, 36, 0.78);
            --mori-audio-button-bg: rgba(108,158,240,0.24);
            --mori-audio-button-color: #8fb6ff;
            --mori-tag-bg: rgba(140, 152, 172, 0.22);
            --mori-pitch-dict-bg: rgba(74, 132, 220, 0.65);
            --mori-freq-border: rgba(108, 158, 240, 0.55);
            --mori-freq-dict-bg: rgba(74, 132, 220, 0.8);
            --mori-summary-color: rgba(190, 196, 210, 0.95);
            --mori-hr-border: rgba(160, 160, 160, 0.22);
            --mori-link-color: #8fb6ff;
            --mori-popup-bg: rgba(31,34,40,0.98);
            --mori-popup-shadow: 0 12px 34px rgba(0,0,0,0.48);
            --mori-image-overlay: rgba(160,160,160,0.8);
        }
        .gloss-image-link[data-appearance="monochrome"] img.gloss-image { filter: brightness(0) invert(1); }
    }
    body { padding: ${topPadding}px 0 ${bottomPadding}px; overflow-x: hidden; }
    #entries-container { padding: $containerPadding; }
    #empty-state { padding: 28px 16px; color: var(--mori-text-secondary); font-size: 15px; text-align: center; }
    #empty-state:empty { display: none; }
    .entry { padding: 10px 10px 12px; border-radius: ${'$'}entryRadius; background: ${'$'}entryBackground; margin-bottom: 8px; }
    $lastEntryMargin
    .entry-header { display: flex; align-items: flex-start; gap: 8px; }
    .header-buttons { display: flex; align-items: center; gap: 6px; margin-left: auto; flex: 0 0 auto; }
    .audio-button, .anki-button { width: 32px; height: 32px; border: none; border-radius: 16px; background: var(--mori-audio-button-bg); color: var(--mori-audio-button-color); font-size: 18px; line-height: 32px; padding: 0; }
    .expression { font-size: 26px; line-height: 1.25; font-weight: 650; overflow-wrap: anywhere; }
    .expression ruby { ruby-align: center; }
    .expression rt { font-size: 13px; font-weight: 400; user-select: none; -webkit-user-select: none; pointer-events: none; }
    .tag-row { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 4px; margin-top: 6px; }
    .expr-tag, .deinflection-tag, .frequency-group { font-size: 11px; border-radius: 4px; padding: 2px 5px; background: var(--mori-tag-bg); line-height: 1.45; }
    .pitch-list { flex: 0 0 100%; width: 100%; margin: 4px 0 0 0; font-size: 13px; }
    .pitch-group { margin-bottom: 4px; }
    .pitch-dict-label { background-color: var(--mori-pitch-dict-bg); color: var(--mori-pitch-dict-text); padding: 2px 5px; border-radius: 4px; font-size: 11px; display: inline-block; }
    .pitch-entries { list-style: none; margin: 3px 0 0 0; padding: 0 0 0 8px; }
    .pitch-entries li { margin-bottom: 2px; }
    .pronunciation-text { font-size: 16px; }
    .pronunciation-mora { position: relative; }
    .pronunciation-mora[data-pitch="high"] > .pronunciation-mora-line { position: absolute; top: -2px; left: 0; right: 0; border-top: 1px solid currentColor; }
    .pronunciation-mora[data-pitch="high"][data-pitch-next="low"] > .pronunciation-mora-line { right: -1px; height: 0.4em; border-right: 1px solid currentColor; }
    .pronunciation-mora[data-pitch="high"][data-pitch-next="low"] { padding-right: 1px; margin-right: 1px; }
    .frequency-group { display: inline-flex; align-items: center; gap: 4px; border: 1px solid var(--mori-freq-border); background: transparent; padding: 0 5px 0 0; overflow: hidden; }
    .frequency-dict-label { align-self: stretch; display: inline-flex; align-items: center; color: var(--mori-freq-dict-text); background: var(--mori-freq-dict-bg); padding: 2px 5px; }
    .frequency-values { font-weight: 600; padding: 2px 0; }
    details.glossary-group { margin-top: 8px; }
    details.glossary-group > summary { cursor: pointer; color: var(--mori-summary-color); font-size: 12px; user-select: none; }
    .glossary-content { margin-top: 4px; line-height: 1.45; font-size: 15px; overflow-wrap: anywhere; }
    .glossary-content ol, .glossary-content ul { padding-left: 1.35em; }
    .compact ul, .compact ol { list-style: none; padding-left: 0; display: inline; }
    .compact li { display: inline; }
    .compact li:not(:last-child)::after { content: " | "; opacity: 0.6; }
    hr { border: none; border-top: 1px solid var(--mori-hr-border); margin: 8px 0; }
    a { color: var(--mori-link-color); }
    table { table-layout: auto; border-collapse: collapse; max-width: 100%; }
    th, td { border: 1px solid currentColor; padding: 0.25em; vertical-align: top; }
    .gloss-image-container { display: inline-block; white-space: nowrap; max-width: 100%; max-height: 100vh; position: relative; vertical-align: top; line-height: 0; font-size: calc(1em / var(--font-size-no-units, 15)); overflow: hidden; }
    .gloss-image-link { cursor: inherit; color: var(--accent-color); display: inline-block; position: relative; line-height: 1; max-width: 100%; }
    .gloss-image-link[data-background=true] > .gloss-image-container { background-color: var(--gloss-image-background-color); }
    .gloss-image-container-overlay { position: absolute; left: 0; top: 0; width: 100%; height: 100%; font-size: calc(1em * var(--font-size-no-units, 15)); line-height: var(--line-height, 1.4); display: table; table-layout: fixed; white-space: normal; color: var(--mori-image-overlay); }
    .gloss-image-background { position: absolute; left: 0; top: 0; width: 100%; height: 100%; background-color: var(--text-color); display: none; }
    .gloss-image { display: inline-block; vertical-align: top; object-fit: contain; border: none; outline: none; max-width: 100%; }
    .gloss-image-link[data-has-aspect-ratio=true] .gloss-image { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
    .gloss-image-link[data-has-aspect-ratio=true] .gloss-image-sizer { display: inline-block; width: 0; vertical-align: top; font-size: 0; }
    .gloss-image-link[data-appearance="monochrome"] img.gloss-image { filter: brightness(0); }
    .gloss-image-link[data-size-units=em] .gloss-image-container { font-size: 1em; }
    .gloss-sc-table-container { display: block; overflow-x: auto; max-width: 100%; }
    #popup-backdrop { position: fixed; inset: 0; z-index: 10; background: transparent; }
    #popup { position: fixed; left: 10px; right: 10px; max-height: min(54vh, 420px); overflow: auto; z-index: 11; border-radius: 10px; background: var(--mori-popup-bg); box-shadow: var(--mori-popup-shadow); padding: 8px; }
    ::highlight(hoshi-selection) { background: var(--mori-selection-bg); }
    """.trimIndent()
}

private fun dictionaryJs(): String = """
const KANJI_RANGE = '\u4E00-\u9FFF\u3400-\u4DBF\uF900-\uFAFF\u3005';
const KANJI_PATTERN = new RegExp(`[${'$'}{KANJI_RANGE}]`);
const KANJI_SEGMENT_PATTERN = new RegExp(`[${'$'}{KANJI_RANGE}]+|[^${'$'}{KANJI_RANGE}]+`, 'g');
const SMALL_KANA_SET = new Set('ぁぃぅぇぉゃゅょゎァィゥェォャュョヮ');
const POS_TAGS = new Set(['n', 'adj-i', 'adj-na', 'adj-no', 'v1', 'vk', 'vs', 'vs-i', 'vs-s', 'vz', 'vi', 'vt']);
const DEFAULT_HARMONIC_RANK = '9999999';
const audioUrls = {};

function el(tag, props = {}, children = []) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props)) {
    if (key === 'styleText') node.style.cssText = value;
    else if (key in node) node[key] = value;
    else node.setAttribute(key, value);
  }
  node.append(...children);
  return node;
}
function toHiragana(text) { return text.replace(/[\u30A1-\u30F6]/g, ch => String.fromCharCode(ch.charCodeAt(0) - 0x60)); }
function toKebabCase(str) { return str.replace(/([A-Z])/g, (_, c, i) => (i ? '-' : '') + c.toLowerCase()); }
function parseTags(tags) { return (tags || '').split(/\s+/).filter(Boolean); }
function isPartOfSpeech(tag) { return POS_TAGS.has(tag); }
function setStructuredContentElementStyle(element, style) {
  for (const [property, value] of Object.entries(style || {})) {
    if ((property === 'marginTop' || property === 'marginLeft' || property === 'marginRight' || property === 'marginBottom') && typeof value === 'number') element.style[property] = `${'$'}{value}em`;
    else element.style[property] = value;
  }
}
function constructDictCss(css, dictName) {
  if (!css) return '';
  const prefix = `.glossary-group [data-dictionary="${'$'}{dictName}"]`;
  const parts = [];
  let i = 0;
  while (i < css.length) {
    while (i < css.length && /\s/.test(css[i])) parts.push(css[i++]);
    if (css.slice(i, i + 2) === '/*') {
      const end = css.indexOf('*/', i + 2);
      if (end === -1) break;
      parts.push(css.slice(i, end + 2));
      i = end + 2;
      continue;
    }
    const bracePos = css.indexOf('{', i);
    if (bracePos === -1) break;
    const selectorPart = css.slice(i, bracePos);
    const selectors = selectorPart.split(',').map(s => {
      const trimmed = s.trim();
      if (!trimmed) return '';
      if (trimmed.startsWith('&') || trimmed.startsWith('@')) return s;
      return `${'$'}{prefix} ${'$'}{trimmed}`;
    });
    parts.push(selectors.join(', '), ' {');
    i = bracePos + 1;
    let depth = 1;
    const blockStart = i;
    while (i < css.length && depth > 0) {
      if (css[i] === '{') depth++;
      else if (css[i] === '}') depth--;
      i++;
    }
    parts.push(css.slice(blockStart, i - 1), '}');
  }
  return parts.join('');
}
function createDefinitionImage(data, dictionary) {
  const {
    path,
    width = 100,
    height = 100,
    preferredWidth,
    preferredHeight,
    title,
    pixelated,
    imageRendering,
    appearance,
    background,
    collapsed,
    collapsible,
    verticalAlign,
    border,
    borderRadius,
    sizeUnits,
    data: nodeData,
  } = data;
  if (!path) return document.createTextNode('');
  const hasPreferredWidth = typeof preferredWidth === 'number';
  const hasPreferredHeight = typeof preferredHeight === 'number';
  const hasDimensions = hasPreferredWidth || hasPreferredHeight || typeof data.width === 'number' || typeof data.height === 'number';
  const invAspectRatio = hasPreferredWidth && hasPreferredHeight ? preferredHeight / preferredWidth : height / width;
  const usedWidth = hasPreferredWidth ? preferredWidth : (hasPreferredHeight ? preferredHeight / invAspectRatio : width);
  const node = el('a', { className: 'gloss-image-link', target: '_blank', rel: 'noreferrer noopener' });
  const imageContainer = el('span', { className: 'gloss-image-container' });
  const aspectRatioSizer = el('span', { className: 'gloss-image-sizer' });
  const imageBackground = el('span', { className: 'gloss-image-background' });
  const overlay = el('span', { className: 'gloss-image-container-overlay' });
  imageContainer.append(aspectRatioSizer, imageBackground, overlay);
  node.appendChild(imageContainer);
  node.dataset.path = path;
  node.dataset.dictionary = dictionary;
  node.dataset.hasAspectRatio = 'true';
  node.dataset.imageRendering = typeof imageRendering === 'string' ? imageRendering : (pixelated ? 'pixelated' : 'auto');
  node.dataset.appearance = typeof appearance === 'string' ? appearance : 'auto';
  node.dataset.background = typeof background === 'boolean' ? `${'$'}{background}` : 'true';
  node.dataset.collapsed = typeof collapsed === 'boolean' ? `${'$'}{collapsed}` : 'false';
  node.dataset.collapsible = typeof collapsible === 'boolean' ? `${'$'}{collapsible}` : 'true';
  if (typeof verticalAlign === 'string') node.dataset.verticalAlign = verticalAlign;
  if (typeof sizeUnits === 'string') node.dataset.sizeUnits = sizeUnits;
  aspectRatioSizer.style.paddingTop = `${'$'}{invAspectRatio * 100}%`;
  if (typeof border === 'string') imageContainer.style.border = border;
  if (typeof borderRadius === 'string') imageContainer.style.borderRadius = borderRadius;
  imageContainer.style.width = `${'$'}{usedWidth}em`;
  if (typeof title === 'string') imageContainer.title = title;
  const img = el('img', {
    className: 'gloss-image',
    alt: nodeData?.alt || title || '',
    src: `image://?dictionary=${'$'}{encodeURIComponent(dictionary)}&path=${'$'}{encodeURIComponent(path)}`,
  });
  if (!hasDimensions) {
    img.addEventListener('load', () => {
      if (img.naturalWidth > 0) {
        imageContainer.style.width = `${'$'}{Math.min(img.naturalWidth, window.innerWidth - 20)}px`;
        aspectRatioSizer.style.paddingTop = `${'$'}{(img.naturalHeight / img.naturalWidth) * 100}%`;
      }
    }, { once: true });
  }
  imageContainer.appendChild(img);
  return node;
}
function segmentFurigana(expression, reading) {
  if (!reading || reading === expression) return [[expression, '']];
  const groups = (expression.match(KANJI_SEGMENT_PATTERN) || []).map(text => ({ text, isKana: !KANJI_PATTERN.test(text[0]), textNormalized: !KANJI_PATTERN.test(text[0]) ? toHiragana(text) : null }));
  function walk(read, normalized, index) {
    if (index >= groups.length) return read.length === 0 ? [] : null;
    const group = groups[index];
    if (group.isKana) {
      if (!normalized.startsWith(group.textNormalized)) return null;
      const rest = walk(read.slice(group.text.length), normalized.slice(group.text.length), index + 1);
      return rest ? [{ text: group.text, reading: '' }, ...rest] : null;
    }
    for (let i = read.length; i >= group.text.length; i--) {
      const rest = walk(read.slice(i), normalized.slice(i), index + 1);
      if (rest) return [{ text: group.text, reading: read.slice(0, i) }, ...rest];
    }
    return null;
  }
  const result = walk(reading, toHiragana(reading), 0);
  return result ? result.map(x => [x.text, x.reading]) : [[expression, reading]];
}
function buildFurigana(parent, expression, reading) {
  for (const [text, rubyText] of segmentFurigana(expression, reading)) {
    if (rubyText) parent.appendChild(el('ruby', {}, [document.createTextNode(text), el('rt', { textContent: rubyText })]));
    else parent.appendChild(document.createTextNode(text));
  }
}
function isRubyAnnotationNode(node) {
  let current = node;
  while (current) {
    if (current.nodeType === Node.ELEMENT_NODE && current.tagName === 'RT') return true;
    current = current.parentNode;
  }
  return false;
}
function renderStructuredContent(parent, content, dictionary) {
  if (content == null) return;
  if (typeof content === 'string' || typeof content === 'number') {
    parent.appendChild(document.createTextNode(String(content)));
    return;
  }
  if (Array.isArray(content)) {
    const isStringArray = content.every(item => typeof item === 'string');
    const insideSpan = parent.tagName === 'SPAN';
    if (isStringArray && content.length > 1 && !insideSpan) {
      const ul = el('ul', { className: 'glossary-list' });
      content.forEach(child => ul.appendChild(el('li', {}, [document.createTextNode(child)])));
      parent.appendChild(ul);
      return;
    }
    content.forEach(item => renderStructuredContent(parent, item?.type === 'structured-content' ? item.content : item, dictionary));
    return;
  }
  if (content.type === 'structured-content') {
    const container = el('span', { className: 'structured-content' });
    parent.appendChild(container);
    renderStructuredContent(container, content.content, dictionary);
    return;
  }
  if (content.type === 'image' || content.tag === 'img') {
    parent.appendChild(createDefinitionImage(content, dictionary));
    return;
  }
  const tag = typeof content.tag === 'string' ? content.tag : 'span';
  const node = el(tag, { className: `gloss-sc-${'$'}{tag}` });
  if (content.href) {
    node.href = content.href;
    node.addEventListener('click', e => { e.preventDefault(); AndroidHoshi.openLink(content.href); });
  }
  if (content.title) node.setAttribute('title', content.title);
  if (content.lang) node.setAttribute('lang', content.lang);
  if (content.data) {
    for (const [k, v] of Object.entries(content.data)) {
      const isCJK = /^[\u3000-\u9FFF\uF900-\uFAFF]/.test(k);
      node.setAttribute(`data-sc${'$'}{isCJK ? '' : '-'}${'$'}{toKebabCase(k)}`, v);
    }
  }
  if (content.style && typeof content.style === 'object') {
    setStructuredContentElementStyle(node, content.style);
  }
  renderStructuredContent(node, content.content ?? content.children ?? content.data?.content ?? '', dictionary);
  if (content.colSpan) node.setAttribute('colspan', content.colSpan);
  if (content.rowSpan) node.setAttribute('rowspan', content.rowSpan);
  if (tag === 'table') {
    parent.appendChild(el('div', { className: 'gloss-sc-table-container' }, [node]));
    return;
  }
  parent.appendChild(node);
}
function createTags(entry) {
  const row = el('div', { className: 'tag-row' });
  if (entry.deinflectionTrace?.length) entry.deinflectionTrace.forEach(step => row.appendChild(el('span', { className: 'deinflection-tag', textContent: step.name })));
  if (window.showExpressionTags) {
    const tags = new Set();
    entry.glossaries?.forEach(g => parseTags(g.termTags).forEach(t => tags.add(t)));
    tags.forEach(t => row.appendChild(el('span', { className: 'expr-tag', textContent: t })));
  }
  if (entry.frequencies?.length) {
    if (window.harmonicFrequency) {
      const normalRow = el('span', { className: 'tag-row', styleText: 'display:none' });
      entry.frequencies.forEach(group => normalRow.appendChild(createFrequencyGroup(group)));
      const harmonic = createHarmonicFrequencyTag(entry.frequencies);
      const toggle = () => {
        const showNormal = normalRow.style.display === 'none';
        normalRow.style.display = showNormal ? '' : 'none';
        harmonic.style.display = showNormal ? 'none' : '';
      };
      normalRow.addEventListener('click', toggle);
      harmonic.addEventListener('click', toggle);
      row.appendChild(harmonic);
      row.appendChild(normalRow);
    } else {
      entry.frequencies.forEach(group => row.appendChild(createFrequencyGroup(group)));
    }
  }
  if (entry.pitches?.length) {
    const pitchContainer = el('div', { className: 'pitch-list' });
    if (window.deduplicatePitchAccents) {
      const seen = new Set();
      entry.pitches.forEach(pitch => {
        const unique = pitch.pitchPositions.filter(pos => !seen.has(pos));
        if (unique.length > 0) {
          unique.forEach(pos => seen.add(pos));
          pitchContainer.appendChild(createPitchGroup({ dictionary: pitch.dictionary, pitchPositions: unique }, entry.reading));
        }
      });
    } else {
      entry.pitches.forEach(pitch => pitchContainer.appendChild(createPitchGroup(pitch, entry.reading)));
    }
    row.appendChild(pitchContainer);
  }
  return row.childNodes.length ? row : null;
}
function getFrequencyHarmonicRank(frequencies) {
  if (!frequencies || frequencies.length === 0) return DEFAULT_HARMONIC_RANK;
  const values = [];
  const seenDictionaries = new Set();
  frequencies.forEach(group => {
    const dictionary = group?.dictionary;
    if (dictionary && seenDictionaries.has(dictionary)) return;
    if (dictionary) seenDictionaries.add(dictionary);
    const firstFreq = group?.frequencies?.[0];
    if (!firstFreq) return;
    const displayValue = firstFreq.displayValue;
    if (displayValue != null) {
      const match = String(displayValue).match(/^\d+/);
      if (match) {
        const parsed = Number.parseInt(match[0], 10);
        if (parsed > 0) {
          values.push(parsed);
          return;
        }
      }
    }
    const value = firstFreq.value;
    if (value && value > 0) values.push(value);
  });
  if (values.length === 0) return DEFAULT_HARMONIC_RANK;
  const sumOfReciprocals = values.reduce((sum, value) => sum + (1 / value), 0);
  return String(Math.floor(values.length / sumOfReciprocals));
}
function createFrequencyGroup(group) {
  const values = (group.frequencies || []).map(freq => freq.displayValue || freq.value).join(', ');
  return el('span', { className: 'frequency-group', title: group.dictionary }, [
    el('span', { className: 'frequency-dict-label', textContent: group.dictionary }),
    el('span', { className: 'frequency-values', textContent: values })
  ]);
}
function isMoraPitchHigh(moraIndex, pitchAccentValue) {
  switch (pitchAccentValue) {
    case 0: return (moraIndex > 0);
    case 1: return (moraIndex < 1);
    default: return (moraIndex > 0 && moraIndex < pitchAccentValue);
  }
}
function getKanaMorae(text) {
  const morae = [];
  let i;
  for (const c of text) {
    if (SMALL_KANA_SET.has(c) && (i = morae.length) > 0) {
      morae[i - 1] += c;
    } else {
      morae.push(c);
    }
  }
  return morae;
}
function getPitchCategory(reading, pitchAccentValue, verbOrAdjective) {
  if (pitchAccentValue === 0) return 'heiban';
  if (verbOrAdjective) return pitchAccentValue > 0 ? 'kifuku' : null;
  if (pitchAccentValue === 1) return 'atamadaka';
  if (pitchAccentValue > 1) {
    const moraCount = getKanaMorae(reading).length;
    return pitchAccentValue >= moraCount ? 'odaka' : 'nakadaka';
  }
  return null;
}
function createPitchHtml(reading, pitchValue) {
  const morae = getKanaMorae(reading);
  const container = el('span', { className: 'pronunciation-text' });
  for (let i = 0; i < morae.length; i++) {
    const mora = morae[i];
    const isHigh = isMoraPitchHigh(i, pitchValue);
    const isHighNext = isMoraPitchHigh(i + 1, pitchValue);
    const moraSpan = el('span', {
      className: 'pronunciation-mora',
      'data-pitch': isHigh ? 'high' : 'low',
      'data-pitch-next': isHighNext ? 'high' : 'low',
      textContent: mora
    });
    moraSpan.appendChild(el('span', { className: 'pronunciation-mora-line' }));
    container.appendChild(moraSpan);
  }
  return container;
}
function createPitchGroup(pitchData, reading) {
  const container = el('div', { className: 'pitch-group' });
  container.appendChild(el('span', { className: 'pitch-dict-label', textContent: pitchData.dictionary }));
  const list = el('ul', { className: 'pitch-entries' });
  pitchData.pitchPositions.forEach((pitch) => {
    const li = el('li');
    li.appendChild(createPitchHtml(reading, pitch));
    li.appendChild(document.createTextNode(` [${'$'}{pitch}]`));
    list.appendChild(li);
  });
  container.appendChild(list);
  return container;
}
function createHarmonicFrequencyTag(frequencies) {
  return el('span', { className: 'frequency-group harmonic-frequency', title: 'Average frequency rank' }, [
    el('span', { className: 'frequency-dict-label', textContent: 'Average' }),
    el('span', { className: 'frequency-values', textContent: getFrequencyHarmonicRank(frequencies) })
  ]);
}
async function fetchAudioUrl(expression, reading) {
  const templates = window.audioSources;
  if (!templates?.length) return null;
  for (const template of templates) {
    const url = template
      .replace('{term}', encodeURIComponent(expression))
      .replace('{reading}', encodeURIComponent(reading || expression));
    try {
      const data = AndroidHoshi.fetchAudioAsync
        ? await fetchAudioSourceList(url)
        : AndroidHoshi.fetchAudioJson
          ? JSON.parse(AndroidHoshi.fetchAudioJson(url))
          : await fetch(`audio://?url=${'$'}{encodeURIComponent(url)}`).then(response => response.json());
      if (data.type === 'audioSourceList' && data.audioSources?.[0]?.url) return data.audioSources[0].url;
    } catch {}
  }
  return null;
}
const pendingAudioFetches = new Map();
function fetchAudioSourceList(url) {
  return new Promise((resolve, reject) => {
    const requestId = `${'$'}{Date.now()}:${'$'}{Math.random().toString(36).slice(2)}`;
    const timeoutId = setTimeout(() => {
      pendingAudioFetches.delete(requestId);
      reject(new Error('audio fetch timeout'));
    }, 22000);
    pendingAudioFetches.set(requestId, payload => {
      clearTimeout(timeoutId);
      pendingAudioFetches.delete(requestId);
      try {
        resolve(JSON.parse(payload));
      } catch (error) {
        reject(error);
      }
    });
    try {
      AndroidHoshi.fetchAudioAsync(requestId, url);
    } catch (error) {
      clearTimeout(timeoutId);
      pendingAudioFetches.delete(requestId);
      reject(error);
    }
  });
}
window.__moriResolveAudioFetch = function(requestId, payload) {
  const resolver = pendingAudioFetches.get(requestId);
  if (resolver) resolver(payload);
};
function playWordAudio(audioUrl) {
  try {
    AndroidHoshi.playWordAudio(JSON.stringify({ url: audioUrl, mode: window.audioPlaybackMode || 'interrupt' }));
    return true;
  } catch {
    return false;
  }
}
function showAudioError(button) {
  button.disabled = false;
  button.textContent = 'x';
  setTimeout(() => { button.textContent = '♪'; }, 1500);
}
function createAudioButton(expression, reading, entryIndex) {
  const audioKey = `${'$'}{entryIndex}:${'$'}{expression}:${'$'}{reading || expression}`;
  const button = el('button', {
    className: 'audio-button',
    textContent: '♪',
    title: '播放发音',
    onclick: async () => {
      if (button.disabled) return;
      button.disabled = true;
      button.textContent = '…';
      try {
        if (!audioUrls[audioKey]) audioUrls[audioKey] = await fetchAudioUrl(expression, reading || expression);
      } catch {
        showAudioError(button);
        return;
      }
      if (!audioUrls[audioKey]) {
        showAudioError(button);
        return;
      }
      button.disabled = false;
      button.textContent = '♪';
      if (!playWordAudio(audioUrls[audioKey])) showAudioError(button);
    }
  });
  return button;
}
function textFromGlossaryContent(content) {
  if (content == null) return '';
  if (typeof content === 'string' || typeof content === 'number') return String(content);
  if (Array.isArray(content)) return content.map(textFromGlossaryContent).filter(Boolean).join('; ');
  if (content.type === 'structured-content') return textFromGlossaryContent(content.content);
  if (content.type === 'image' || content.tag === 'img') return content.title || content.data?.alt || '';
  return textFromGlossaryContent(content.content ?? content.children ?? content.data?.content ?? '');
}
function glossaryText(item) {
  try { return textFromGlossaryContent(JSON.parse(item.content)); } catch { return textFromGlossaryContent(item.content); }
}
function pitchCategories(entry) {
  const tags = parseTags((entry.glossaries || []).map(g => g.termTags).join(' '));
  const verbOrAdjective = tags.some(isPartOfSpeech);
  const categories = new Set();
  (entry.pitches || []).forEach(group => (group.pitchPositions || []).forEach(position => {
    const category = getPitchCategory(entry.reading || entry.expression, position, verbOrAdjective);
    if (category) categories.add(category);
  }));
  return Array.from(categories).join(', ');
}
function ankiPayload(entry) {
  const glossaries = (entry.glossaries || []).map(glossaryText).filter(Boolean);
  const frequencies = (entry.frequencies || []).map(group => {
    const values = (group.frequencies || []).map(freq => freq.displayValue || freq.value).filter(Boolean).join(', ');
    return values ? `${'$'}{group.dictionary}: ${'$'}{values}` : group.dictionary;
  }).filter(Boolean).join('; ');
  const pitchPositions = (entry.pitches || [])
    .flatMap(group => group.pitchPositions || [])
    .filter((value, index, list) => list.indexOf(value) === index)
    .join(', ');
  return {
    expression: entry.expression || '',
    reading: entry.reading || '',
    furiganaPlain: entry.reading ? `${'$'}{entry.expression}[${'$'}{entry.reading}]` : (entry.expression || ''),
    glossary: glossaries.join('\n'),
    glossaryFirst: glossaries[0] || '',
    selectedGlossary: glossaries[0] || '',
    popupSelectionText: window.lastPopupSelectionText || '',
    sentence: window.lastPopupSelectionText || window.lookupQuery || '',
    frequencies,
    frequencyHarmonicRank: getFrequencyHarmonicRank(entry.frequencies),
    pitchAccentPositions: pitchPositions,
    pitchAccentCategories: pitchCategories(entry),
    documentTitle: '',
    audio: []
  };
}
function createAnkiButton(entry) {
  return el('button', {
    className: 'anki-button',
    textContent: '+',
    title: '添加到 Anki',
    onclick: () => {
      try { AndroidHoshi.addAnkiCard(JSON.stringify(ankiPayload(entry))); } catch {}
    }
  });
}
function createEntry(entry, index) {
  const entryDiv = el('div', { className: 'entry' });
  const expression = el('span', { className: 'expression' });
  buildFurigana(expression, entry.expression, entry.reading);
  const headerChildren = [expression];
  const buttons = [];
  if (window.ankiEnabled) buttons.push(createAnkiButton(entry));
  if (window.audioSources?.length) buttons.push(createAudioButton(entry.expression, entry.reading, index));
  if (buttons.length) {
    headerChildren.push(el('div', { className: 'header-buttons' }, buttons));
  }
  entryDiv.appendChild(el('div', { className: 'entry-header' }, headerChildren));
  const tags = createTags(entry);
  if (tags) entryDiv.appendChild(tags);
  const grouped = {};
  entry.glossaries?.forEach(g => (grouped[g.dictionary] ??= []).push(g));
  Object.entries(grouped).forEach(([dictName, items], dictIndex) => {
    const details = el('details', { className: 'glossary-group' });
    details.open = !window.collapseDictionaries || dictIndex === 0;
    details.appendChild(el('summary', { textContent: dictName }));
    const wrapper = el('div', { className: window.compactGlossaries ? 'compact' : '' });
    wrapper.setAttribute('data-dictionary', dictName);
    if (window.dictionaryStyles?.[dictName]) wrapper.appendChild(el('style', { textContent: constructDictCss(window.dictionaryStyles[dictName], dictName) }));
    items.forEach(item => {
      const content = el('div', { className: 'glossary-content' });
      const tags = parseTags(item.definitionTags).filter(t => !/^\d+$/.test(t) && !isPartOfSpeech(t));
      if (tags.length) content.appendChild(el('div', { className: 'tag-row' }, tags.map(t => el('span', { className: 'expr-tag', textContent: t }))));
      try { renderStructuredContent(content, JSON.parse(item.content), dictName); } catch { renderStructuredContent(content, item.content, dictName); }
      wrapper.appendChild(content);
    });
    details.appendChild(wrapper);
    entryDiv.appendChild(details);
  });
  return entryDiv;
}
function renderEntries(entries, target) {
  target.innerHTML = '';
  entries.forEach((entry, index) => {
    if (index > 0) target.appendChild(el('hr'));
    const entryDiv = createEntry(entry, index);
    target.appendChild(entryDiv);
    if (window.audioEnableAutoplay && window.audioSources?.length && index === 0) {
      const autoplayKey = `${'$'}{window.lookupQuery || ''}:${'$'}{entry.expression}:${'$'}{entry.reading || ''}:${'$'}{entry.matched || ''}`;
      if (!AndroidHoshi.consumeAutoplay || AndroidHoshi.consumeAutoplay(autoplayKey)) {
        setTimeout(() => entryDiv.querySelector('.audio-button')?.click(), 70);
      }
    }
  });
}
window.hoshiSelection = {
    selection: null,
    scanDelimiters: '。、！？…‥「」『』（）()【】〈〉《》〔〕｛｝{}［］[]・：；:;，,.─\n\r',
    sentenceDelimiters: '。！？.!?\n\r',
    trailingSentenceChars: '。、！？…‥」』）)】〉》〕｝}］]',
    brackets: {'「':'」', '『': '』', '（':'）', '(':')', '【':'】', '〈':'〉', '《':'》', '〔':'〕', '｛':'｝', '{':'}', '［':'］', '[':']'},

    isScanBoundary(char) {
        return /^[\s\u3000]$/.test(char) || this.scanDelimiters.includes(char);
    },

    isFurigana(node) {
        const el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
        return !!el?.closest('rt, rp');
    },

    findParagraph(node) {
        let el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
        return el?.closest('p, .glossary-content') || null;
    },

    createWalker(rootNode) {
        const root = rootNode || document.body;
        return document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
            acceptNode: (n) => this.isFurigana(n) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT
        });
    },

    inCharRange(charRange, x, y) {
        const rects = charRange.getClientRects();
        if (rects.length) {
            for (const rect of rects) {
                if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                    return true;
                }
            }
            return false;
        }
        const rect = charRange.getBoundingClientRect();
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    },

    getCaretRange(x, y) {
        if (document.caretPositionFromPoint) {
            const pos = document.caretPositionFromPoint(x, y);
            if (!pos) return null;
            const range = document.createRange();
            range.setStart(pos.offsetNode, pos.offset);
            range.collapse(true);
            return range;
        } else {
            const element = document.elementFromPoint(x, y);
            if (!element) return null;

            const container = element.closest('p, div, span, ruby, a') || document.body;
            const walker = this.createWalker(container);

            const range = document.createRange();
            let node;
            while (node = walker.nextNode()) {
                for (let i = 0; i < node.textContent.length; i++) {
                    range.setStart(node, i);
                    range.setEnd(node, i + 1);
                    if (this.inCharRange(range, x, y)) {
                        range.collapse(true);
                        return range;
                    }
                }
            }
            return document.caretRangeFromPoint(x, y);
        }
    },

    getCharacterAtPoint(x, y) {
        const range = this.getCaretRange(x, y);
        if (!range) return null;

        const node = range.startContainer;
        if (node.nodeType !== Node.TEXT_NODE) return null;
        if (this.isFurigana(node)) return null;

        const text = node.textContent;
        const caret = range.startOffset;

        for (const offset of [caret, caret - 1, caret + 1]) {
            if (offset < 0 || offset >= text.length) continue;

            const charRange = document.createRange();
            charRange.setStart(node, offset);
            charRange.setEnd(node, offset + 1);
            if (this.inCharRange(charRange, x, y)) {
                if (this.isScanBoundary(text[offset])) return null;
                return { node, offset };
            }
        }

        return null;
    },

    getSentence(startNode, startOffset) {
        const container = this.findParagraph(startNode) || document.body;
        const walker = this.createWalker(container);

        walker.currentNode = startNode;
        const partsBefore = [];
        let node = startNode;
        let limit = startOffset;

        while (node) {
            const text = node.textContent;
            let foundStart = false;
            for (let i = limit - 1; i >= 0; i--) {
                if (this.sentenceDelimiters.includes(text[i])) {
                    partsBefore.push(text.slice(i + 1, limit));
                    foundStart = true;
                    break;
                }
            }

            if (foundStart) break;

            partsBefore.push(text.slice(0, limit));
            node = walker.previousNode();
            if (node) limit = node.textContent.length;
        }

        walker.currentNode = startNode;
        const partsAfter = [];
        node = startNode;
        let start = startOffset;

        while (node) {
            const text = node.textContent;
            let foundEnd = false;

            for (let i = start; i < text.length; i++) {
                if (this.sentenceDelimiters.includes(text[i])) {
                    let end = i + 1;
                    while (end < text.length) {
                        if (!this.trailingSentenceChars.includes(text[end])) break;
                        end += 1;
                    }
                    partsAfter.push(text.slice(start, end));
                    foundEnd = true;
                    break;
                }
            }

            if (foundEnd) break;

            partsAfter.push(text.slice(start));
            node = walker.nextNode();
            start = 0;
        }

        let sentence = (partsBefore.reverse().join('') + partsAfter.join('')).trim();
        const closeBrackets = new Set(Object.values(this.brackets));
        const openBrackets = new Set(Object.keys(this.brackets));
        let stack = [];
        let unmatchedClose = [];

        for (let i = 0; i < sentence.length; i++) {
            const ch = sentence[i];
            if (openBrackets.has(ch)) {
                stack.push(ch);
            } else if (closeBrackets.has(ch)) {
                if (stack.length > 0 && this.brackets[stack[stack.length - 1]] === ch) {
                    stack.pop();
                } else {
                    unmatchedClose.push(ch);
                }
            }
        }

        let startSlice = 0;
        while (stack.length > 0 && startSlice < sentence.length - 1) {
            if (stack[0] === sentence[startSlice]) {
                stack.shift();
            } else break;
            startSlice++;
        }

        let endSlice = sentence.length - 1;
        let endIdx = sentence.length - 1;
        while (unmatchedClose.length > 0 && endIdx > startSlice) {
            if (unmatchedClose[unmatchedClose.length - 1] === sentence[endIdx]) {
                unmatchedClose.pop();
                endSlice = endIdx - 1;
            } else if (!this.sentenceDelimiters.includes(sentence[endIdx])) break;
            endIdx--;
        }
        return sentence.slice(startSlice, endSlice + 1).trim();
    },

    selectText(x, y, maxLength) {
        const hit = this.getCharacterAtPoint(x, y);
        if (!hit) {
            this.clearSelection();
            return null;
        }

        if (this.selection &&
            hit.node === this.selection.startNode &&
            hit.offset === this.selection.startOffset) {
            this.clearSelection();
            return null;
        }

        this.clearSelection();

        const container = this.findParagraph(hit.node) || document.body;
        const walker = this.createWalker(container);

        let text = '';
        let node = hit.node;
        let offset = hit.offset;
        let ranges = [];

        walker.currentNode = node;
        while (text.length < maxLength && node) {
            const content = node.textContent;
            const start = offset;

            while (offset < content.length && text.length < maxLength) {
                const char = content[offset];
                if (this.isScanBoundary(char)) break;
                text += char;
                offset++;
            }

            if (offset > start) {
                ranges.push({ node, start, end: offset });
            }

            if (offset < content.length || text.length >= maxLength) break;

            node = walker.nextNode();
            offset = 0;
        }

        if (!text) return null;

        this.selection = {
            startNode: hit.node,
            startOffset: hit.offset,
            ranges,
            text
        };

        return {
            text,
            sentence: this.getSentence(hit.node, hit.offset),
            rect: this.getSelectionRect(x, y)
        };
    },

    getSelectionRect(x, y) {
        if (!this.selection?.ranges.length) return null;

        const first = this.selection.ranges[0];
        const range = document.createRange();
        range.setStart(first.node, first.start);
        range.setEnd(first.node, first.start + 1);

        const rects = Array.from(range.getClientRects());
        const rect = rects.find(rect => x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) ?? range.getBoundingClientRect();
        return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
    },

    highlightSelection(charCount) {
        if (!this.selection?.ranges.length) return;

        const highlights = [];
        let remaining = charCount;

        for (const r of this.selection.ranges) {
            if (remaining <= 0) break;

            let end = r.start;
            while (end < r.end && remaining > 0) {
                const char = String.fromCodePoint(r.node.textContent.codePointAt(end));
                end += char.length;
                remaining--;
            }

            const range = document.createRange();
            range.setStart(r.node, r.start);
            range.setEnd(r.node, end);
            highlights.push(range);
        }

        CSS.highlights?.set('hoshi-selection', new Highlight(...highlights));
    },

    clearSelection() {
        window.getSelection()?.removeAllRanges();
        CSS.highlights?.get('hoshi-selection')?.clear();
        this.selection = null;
    }
};
function hidePopup() {
  document.getElementById('popup').hidden = true;
  document.getElementById('popup-backdrop').hidden = true;
  window.hoshiSelection.clearSelection();
}
function showPopup(selection) {
  window.lastPopupSelectionText = selection.text || '';
  const result = JSON.parse(AndroidHoshi.lookup(selection.text, window.maxResults || 16));
  if (!result.entries?.length) { hidePopup(); return; }
  const matched = result.entries[0]?.matched || '';
  if (matched) window.hoshiSelection.highlightSelection(Array.from(matched).length);
  const popup = document.getElementById('popup');
  const content = document.getElementById('popup-content');
  const oldEntries = window.lookupEntries;
  const oldStyles = window.dictionaryStyles;
  window.lookupEntries = result.entries;
  window.dictionaryStyles = result.styles || {};
  renderEntries(result.entries, content);
  normalizeRubyTextContainers(content);
  window.lookupEntries = oldEntries;
  window.dictionaryStyles = oldStyles;
  const yBelow = selection.rect.bottom + 8;
  const desiredTop = yBelow + popup.offsetHeight > window.innerHeight ? Math.max(8, selection.rect.top - popup.offsetHeight - 8) : yBelow;
  popup.style.top = `${'$'}{desiredTop}px`;
  popup.hidden = false;
  document.getElementById('popup-backdrop').hidden = false;
}
function highlightLookupMatch(selection) {
  try {
    const result = JSON.parse(AndroidHoshi.lookup(selection.text, window.maxResults || 16));
    const matched = result.entries?.[0]?.matched || '';
    if (matched) window.hoshiSelection.highlightSelection(Array.from(matched).length);
  } catch {}
}
function normalizeRubyTextContainers(root) {
  root.querySelectorAll('.glossary-content ruby, .expression ruby').forEach(ruby => {
    ruby.childNodes.forEach(node => {
      if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
        const span = document.createElement('span');
        span.textContent = node.textContent;
        node.replaceWith(span);
      }
    });
  });
}
document.getElementById('popup-backdrop').addEventListener('click', hidePopup);
if (window.swipeThreshold) {
  let swipeStartX = 0;
  let swipeStartY = 0;
  document.addEventListener('touchstart', e => {
    swipeStartX = e.touches[0]?.clientX ?? 0;
    swipeStartY = e.touches[0]?.clientY ?? 0;
  });
  document.addEventListener('touchend', e => {
    const touch = e.changedTouches[0];
    if (!touch) return;
    const dx = touch.clientX - swipeStartX;
    const dy = touch.clientY - swipeStartY;
    const hasSelection = window.getSelection().toString();
    if (Math.abs(dx) > window.swipeThreshold && Math.abs(dy) < 20 && !hasSelection) {
      try { AndroidHoshi.swipeDismiss(); } catch {}
    }
  });
}
document.addEventListener('click', e => {
  if (window.enableInternalPopup && e.target.closest('#popup')) return;
  const target = e.target?.nodeType === Node.TEXT_NODE ? e.target.parentElement : e.target;
  if (!target?.closest('.glossary-content') && !target?.closest('.expression') && !target?.closest('.expr-tag')) {
    if (window.enableInternalPopup) hidePopup();
    return;
  }
  const selection = window.hoshiSelection.selectText(e.clientX, e.clientY, window.scanLength || 16);
  if (selection) {
    if (window.enableInternalPopup) {
      showPopup(selection);
    } else {
      highlightLookupMatch(selection);
      try { AndroidHoshi.openChildPopup(JSON.stringify(selection)); } catch {}
    }
  } else if (window.enableInternalPopup) {
    hidePopup();
  }
});
const container = document.getElementById('entries-container');
if (window.lookupEntries.length) {
  renderEntries(window.lookupEntries, container);
  normalizeRubyTextContainers(container);
} else {
  document.getElementById('empty-state').textContent = window.emptyMessage || '';
}
""".trimIndent()

private class AndroidWordAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    fun play(context: Context, url: String, mode: AudioPlaybackMode) {
        stop(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (mode != AudioPlaybackMode.Mix && !requestFocus(audioManager, mode)) return

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setDataSource(context, Uri.parse(url))
            setOnCompletionListener { stop(context) }
            setOnErrorListener { _, _, _ ->
                stop(context)
                true
            }
            prepareAsync()
            setOnPreparedListener { it.start() }
        }
        mediaPlayer = player
    }

    private fun requestFocus(audioManager: AudioManager, mode: AudioPlaybackMode): Boolean {
        val gain = when (mode) {
            AudioPlaybackMode.Interrupt -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            AudioPlaybackMode.Duck -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            AudioPlaybackMode.Mix -> return true
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(gain)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                gain
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun stop(context: Context) {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
                player.release()
            }
        }
        mediaPlayer = null
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }
}

private fun mimeTypeFor(path: String): String =
    when (path.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
