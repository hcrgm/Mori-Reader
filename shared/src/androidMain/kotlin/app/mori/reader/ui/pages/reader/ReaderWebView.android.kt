package app.mori.reader.ui.pages.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.mori.reader.data.audiobook.SasayakiCueRange
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import org.json.JSONObject
import kotlin.math.abs

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
actual fun ReaderWebView(
    state: ReaderWebViewState,
    config: ReaderWebViewSettings,
    modifier: Modifier,
    callbacks: ReaderWebViewCallbacks,
) {
    val chapter = state.chapter
    val progress = state.progress
    val navigationVersion = state.navigationVersion
    val fragment = state.fragment
    val selectionHighlightLength = state.selectionHighlightLength
    val sasayakiCues = state.sasayakiCues
    val highlightedSasayakiCueId = state.highlightedSasayakiCueId
    val verticalWriting = config.verticalWriting
    val isDark = config.isDark
    val scanLength = config.scanLength
    val fontSize = config.fontSize
    val lineHeight = config.lineHeight
    val horizontalPadding = config.horizontalPadding
    val verticalPadding = config.verticalPadding
    val avoidPageBreak = config.avoidPageBreak
    val justifyText = config.justifyText
    val characterSpacing = config.characterSpacing
    val continuousMode = config.continuousMode
    val hideFurigana = config.hideFurigana
    val sasayakiAutoScroll = config.sasayakiAutoScroll
    val sasayakiHighlightEnabled = config.sasayakiHighlightEnabled
    val sasayakiHighlightColor = config.sasayakiHighlightColor
    val stabilizeForBackdrop = config.stabilizeForBackdrop
    val onProgressChanged = callbacks.onProgressChanged
    val onProgressSaved = callbacks.onProgressSaved
    val onTextSelected = callbacks.onTextSelected
    val onLinkActivated = callbacks.onLinkActivated
    val onTapOutside = callbacks.onTapOutside
    val onNextChapter = callbacks.onNextChapter
    val onPreviousChapter = callbacks.onPreviousChapter
    val currentProgressChanged = rememberUpdatedState(onProgressChanged)
    val currentProgressSaved = rememberUpdatedState(onProgressSaved)
    val currentTextSelected = rememberUpdatedState(onTextSelected)
    val currentLinkActivated = rememberUpdatedState(onLinkActivated)
    val currentTapOutside = rememberUpdatedState(onTapOutside)
    val currentNextChapter = rememberUpdatedState(onNextChapter)
    val currentPreviousChapter = rememberUpdatedState(onPreviousChapter)
    val bridge = remember { ReaderBridge() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MoriReaderWebView(context).apply {
                var lastProgressUpdate = 0L
                val handler = Handler(Looper.getMainLooper())
                var saveProgressRunnable: Runnable? = null
                var nativeSelectionArmed = false
                var nativeSelectionRunnable: Runnable? = null

                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(Color.TRANSPARENT)
                alpha = 0f
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = false
                addJavascriptInterface(bridge, "AndroidMoriReader")
                webViewClient =
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            val href = request.url?.toString().orEmpty()
                            if (!href.isInternalReaderHref()) return false
                            bridge.onLinkActivated(href)
                            return true
                        }

                        override fun onPageFinished(
                            view: WebView,
                            url: String?,
                        ) {
                            view.evaluateJavascript(
                                readerBootstrapScript(
                                    progress = bridge.progress,
                                    fragment = bridge.fragment,
                                    verticalWriting = bridge.verticalWriting,
                                    isDark = bridge.isDark,
                                    fontSize = bridge.fontSize,
                                    lineHeight = bridge.lineHeight,
                                    horizontalPadding = bridge.horizontalPadding,
                                    verticalPadding = bridge.verticalPadding,
                                    avoidPageBreak = bridge.avoidPageBreak,
                                    justifyText = bridge.justifyText,
                                    characterSpacing = bridge.characterSpacing,
                                    continuousMode = bridge.continuousMode,
                                    hideFurigana = bridge.hideFurigana,
                                    sasayakiCues = bridge.sasayakiCues,
                                    highlightedSasayakiCueId = bridge.highlightedSasayakiCueId,
                                    sasayakiAutoScroll = bridge.sasayakiAutoScroll,
                                    sasayakiHighlightEnabled = bridge.sasayakiHighlightEnabled,
                                    sasayakiHighlightColor = bridge.sasayakiHighlightColor,
                                ),
                                null,
                            )
                        }
                    }
                setOnScrollChangeListener { view, _, _, _, _ ->
                    if (!bridge.continuousMode || bridge.isRestoring) return@setOnScrollChangeListener
                    val now = SystemClock.uptimeMillis()
                    if (now - lastProgressUpdate >= 50L) {
                        lastProgressUpdate = now
                        (view as WebView).evaluateJavascript("window.moriReader.calculateProgress()") { result ->
                            result
                                ?.toDoubleOrNull()
                                ?.let { bridge.onProgressChanged(it.coerceIn(0.0, 1.0)) }
                        }
                    }
                    saveProgressRunnable?.let(handler::removeCallbacks)
                    saveProgressRunnable =
                        Runnable {
                            (view as WebView).evaluateJavascript("window.moriReader.calculateProgress()") { result ->
                                result
                                    ?.toDoubleOrNull()
                                    ?.let { bridge.onProgressSaved(it.coerceIn(0.0, 1.0)) }
                            }
                        }.also { handler.postDelayed(it, 250L) }
                }
                setOnTouchListener(
                    ReaderGestureTouchListener(
                        context = context,
                        onTap = { x, y ->
                            handleTapSelection(
                                webView = this@apply,
                                x = x,
                                y = y,
                                scanLength = bridge.scanLength,
                                onTapOutside = { currentTapOutside.value() },
                            )
                        },
                        onLeftSwipe = {
                            handleHorizontalSwipe(
                                webView = this@apply,
                                swipeLeft = true,
                                continuousMode = bridge.continuousMode,
                                verticalWriting = bridge.verticalWriting,
                                onNextChapter = { currentNextChapter.value() },
                                onPreviousChapter = { currentPreviousChapter.value() },
                            )
                        },
                        onRightSwipe = {
                            handleHorizontalSwipe(
                                webView = this@apply,
                                swipeLeft = false,
                                continuousMode = bridge.continuousMode,
                                verticalWriting = bridge.verticalWriting,
                                onNextChapter = { currentNextChapter.value() },
                                onPreviousChapter = { currentPreviousChapter.value() },
                            )
                        },
                    ),
                )
            }
        },
        update = { webView ->
            bridge.onProgressChanged = { currentProgressChanged.value(it) }
            bridge.onProgressSaved = { currentProgressSaved.value(it) }
            bridge.onTextSelected =
                { text, sentence, rect -> currentTextSelected.value(text, sentence, rect) }
            bridge.onLinkActivated = { href -> currentLinkActivated.value(href) }
            bridge.progress = progress.coerceIn(0.0, 1.0)
            bridge.fragment = fragment
            bridge.verticalWriting = verticalWriting
            bridge.isDark = isDark
            bridge.scanLength = scanLength
            bridge.fontSize = fontSize
            bridge.lineHeight = lineHeight
            bridge.horizontalPadding = horizontalPadding
            bridge.verticalPadding = verticalPadding
            bridge.avoidPageBreak = avoidPageBreak
            bridge.justifyText = justifyText
            bridge.characterSpacing = characterSpacing
            bridge.continuousMode = continuousMode
            bridge.hideFurigana = hideFurigana
            bridge.sasayakiCues = sasayakiCues
            bridge.highlightedSasayakiCueId = highlightedSasayakiCueId
            bridge.sasayakiAutoScroll = sasayakiAutoScroll
            bridge.sasayakiHighlightEnabled = sasayakiHighlightEnabled
            bridge.sasayakiHighlightColor = sasayakiHighlightColor
            bridge.webView = webView
            val targetLayerType =
                if (stabilizeForBackdrop) {
                    View.LAYER_TYPE_SOFTWARE
                } else {
                    View.LAYER_TYPE_NONE
                }
            if (webView.layerType != targetLayerType) {
                webView.setLayerType(targetLayerType, null)
            }
            val sourceUrl = chapter?.sourceUrl
            val key =
                listOf(
                    sourceUrl,
                    navigationVersion,
                    verticalWriting,
                    isDark,
                    fontSize,
                    lineHeight,
                    horizontalPadding,
                    verticalPadding,
                    avoidPageBreak,
                    justifyText,
                    characterSpacing,
                    continuousMode,
                    hideFurigana,
                    sasayakiCues.joinToString(";") { "${it.id}:${it.start}:${it.length}" },
                    sasayakiAutoScroll,
                    sasayakiHighlightEnabled,
                    sasayakiHighlightColor,
                ).joinToString("|")
            if (sourceUrl != null && webView.tag != key) {
                webView.tag = key
                bridge.isRestoring = true
                bridge.appliedSelectionHighlightLength = null
                bridge.appliedHighlightedSasayakiCueId = null
                webView.alpha = 0f
                webView.loadUrl(sourceUrl)
            } else if (bridge.appliedSelectionHighlightLength != selectionHighlightLength) {
                bridge.appliedSelectionHighlightLength = selectionHighlightLength
                val script =
                    selectionHighlightLength
                        ?.takeIf { it > 0 }
                        ?.let { "window.moriSelection && window.moriSelection.highlightSelection($it)" }
                        ?: "window.moriSelection && window.moriSelection.clearCustomHighlight()"
                webView.evaluateJavascript(script, null)
            } else if (bridge.appliedHighlightedSasayakiCueId != highlightedSasayakiCueId) {
                bridge.appliedHighlightedSasayakiCueId = highlightedSasayakiCueId
                val script =
                    highlightedSasayakiCueId
                        ?.let { "window.moriSasayaki && window.moriSasayaki.highlightSasayakiCue(${it.jsString()})" }
                        ?: "window.moriSasayaki && window.moriSasayaki.clearSasayakiCue()"
                webView.evaluateJavascript(script, null)
            }
        },
    )
}

private class MoriReaderWebView(
    context: Context,
) : WebView(context) {
    @Suppress("DEPRECATION")
    fun toJsViewportPoint(
        x: Float,
        y: Float,
    ): Pair<Float, Float> {
        val currentScale = scale.takeIf { it > 0f } ?: 1f
        return (x / currentScale) to (y / currentScale)
    }

    fun isAtContinuousBoundary(
        direction: String,
        verticalWriting: Boolean,
        threshold: Int,
    ): Boolean {
        val offset =
            if (verticalWriting) {
                computeHorizontalScrollOffset()
            } else {
                computeVerticalScrollOffset()
            }
        val range =
            if (verticalWriting) {
                computeHorizontalScrollRange()
            } else {
                computeVerticalScrollRange()
            }
        val extent =
            if (verticalWriting) {
                computeHorizontalScrollExtent()
            } else {
                computeVerticalScrollExtent()
            }
        val maxOffset = (range - extent).coerceAtLeast(0)
        return when (direction) {
            "forward" -> {
                if (verticalWriting) {
                    offset <= threshold
                } else {
                    offset >= maxOffset - threshold
                }
            }

            "backward" -> {
                if (verticalWriting) {
                    offset >= maxOffset - threshold
                } else {
                    offset <= threshold
                }
            }

            else -> {
                false
            }
        }
    }
}

private class ReaderGestureTouchListener(
    context: Context,
    private val onTap: (x: Float, y: Float) -> Unit = { _, _ -> },
    private val onLeftSwipe: () -> Unit = {},
    private val onRightSwipe: () -> Unit = {},
) : View.OnTouchListener {
    private val detector = GestureDetector(context, GestureListener())

    override fun onTouch(
        view: View,
        event: MotionEvent,
    ): Boolean {
        detector.onTouchEvent(event)
        return false
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean = true

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            onTap(event.x, event.y)
            return true
        }

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            val start = downEvent ?: return false
            val dx = moveEvent.x - start.x
            val dy = moveEvent.y - start.y
            if (abs(dx) < MIN_DISTANCE || abs(dx) < abs(dy)) return false
            if (dx < 0) onLeftSwipe() else onRightSwipe()
            return true
        }
    }

    private companion object {
        const val MIN_DISTANCE = 72f
    }
}

private fun handleTapSelection(
    webView: MoriReaderWebView,
    x: Float,
    y: Float,
    scanLength: Int,
    onTapOutside: () -> Unit,
) {
    if (webView.hitTestIsInternalLink()) return
    val (jsX, jsY) = webView.toJsViewportPoint(x, y)
    val script =
        """
        (function() {
          var href = window.moriReader.linkAtPoint($jsX, $jsY);
          if (href) {
            AndroidMoriReader.linkActivated(href);
            return 'link';
          }
          return window.moriSelection.selectText($jsX, $jsY, $scanLength);
        })()
        """.trimIndent()
    webView.evaluateJavascript(script) { result ->
        if (result == null || result == "null") {
            onTapOutside()
        }
    }
}

private fun handleHorizontalSwipe(
    webView: MoriReaderWebView,
    swipeLeft: Boolean,
    continuousMode: Boolean,
    verticalWriting: Boolean,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
) {
    if (continuousMode) {
        val direction =
            if (verticalWriting) {
                if (swipeLeft) "backward" else "forward"
            } else {
                if (swipeLeft) "forward" else "backward"
            }
        val atBoundary =
            webView.isAtContinuousBoundary(
                direction = direction,
                verticalWriting = verticalWriting,
                threshold = 24,
            )
        if (!atBoundary) return
        if (direction == "forward") onNextChapter() else onPreviousChapter()
        return
    }

    val direction =
        if (verticalWriting) {
            if (swipeLeft) "backward" else "forward"
        } else {
            if (swipeLeft) "forward" else "backward"
        }
    webView.evaluateJavascript("window.moriReader.paginate('$direction')") { result ->
        if (result?.contains("limit") == true) {
            if (direction == "forward") onNextChapter() else onPreviousChapter()
        }
    }
}

private class ReaderBridge {
    var progress: Double = 0.0
    var fragment: String? = null
    var verticalWriting: Boolean = true
    var isDark: Boolean = false
    var scanLength: Int = 16
    var fontSize: Int = 22
    var lineHeight: Double = 1.65
    var horizontalPadding: Int = 5
    var verticalPadding: Int = 0
    var avoidPageBreak: Boolean = false
    var justifyText: Boolean = false
    var characterSpacing: Double = 0.0
    var continuousMode: Boolean = false
    var hideFurigana: Boolean = false
    var sasayakiCues: List<SasayakiCueRange> = emptyList()
    var highlightedSasayakiCueId: String? = null
    var sasayakiAutoScroll: Boolean = true
    var sasayakiHighlightEnabled: Boolean = true
    var sasayakiHighlightColor: String = "#FFC0485C"
    var appliedHighlightedSasayakiCueId: String? = null
    var appliedSelectionHighlightLength: Int? = null
    var isRestoring: Boolean = true
    var webView: WebView? = null
    var onProgressChanged: (Double) -> Unit = {}
    var onProgressSaved: (Double) -> Unit = {}
    var onTextSelected: (String, String, ReaderSelectionRect?) -> Unit = { _, _, _ -> }
    var onLinkActivated: (String) -> Unit = {}

    @JavascriptInterface
    fun progressChanged(progress: Double) {
        onProgressChanged(progress.coerceIn(0.0, 1.0))
    }

    @JavascriptInterface
    fun progressSaved(progress: Double) {
        onProgressSaved(progress.coerceIn(0.0, 1.0))
    }

    @JavascriptInterface
    fun textSelected(payload: String) {
        val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
        val rectJson = json.optJSONObject("rect")
        val rect =
            rectJson?.let {
                ReaderSelectionRect(
                    x = it.optDouble("x").toFloat(),
                    y = it.optDouble("y").toFloat(),
                    width = it.optDouble("width").toFloat(),
                    height = it.optDouble("height").toFloat(),
                    normalizedOffset = if (it.has("normalizedOffset")) it.optInt("normalizedOffset") else null,
                )
            }
        onTextSelected(
            json.optString("text"),
            json.optString("sentence"),
            rect,
        )
    }

    @JavascriptInterface
    fun linkActivated(href: String) {
        onLinkActivated(href)
    }

    @JavascriptInterface
    fun restoreCompleted() {
        isRestoring = false
        webView?.post {
            webView
                ?.animate()
                ?.alpha(1f)
                ?.setDuration(250L)
                ?.start()
        }
    }
}

private fun WebView.hitTestIsInternalLink(): Boolean {
    val result = hitTestResult
    val href = result.extra?.trim().orEmpty()
    if (href.isBlank()) return false
    val isLink =
        when (result.type) {
            WebView.HitTestResult.SRC_ANCHOR_TYPE,
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE,
            -> true

            else -> false
        }
    return isLink && href.isInternalReaderHref()
}

private fun String.isInternalReaderHref(): Boolean {
    val trimmed = trim()
    if (trimmed.isBlank()) return false
    val schemeEnd = trimmed.indexOf(':')
    if (schemeEnd <= 0) return true
    val scheme = trimmed.take(schemeEnd)
    val validScheme =
        scheme.withIndex().all { (index, char) ->
            if (index == 0) char.isLetter() else char.isLetterOrDigit() || char == '+' || char == '.' || char == '-'
        }
    return !validScheme || scheme.equals("file", ignoreCase = true)
}
