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
import app.mori.reader.data.book.ReaderChapter
import app.mori.reader.ui.ReaderSelectionRect
import org.json.JSONObject
import kotlin.math.abs

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
actual fun ReaderWebView(
    chapter: ReaderChapter?,
    progress: Double,
    navigationVersion: Int,
    fragment: String?,
    verticalWriting: Boolean,
    isDark: Boolean,
    scanLength: Int,
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    characterSpacing: Double,
    continuousMode: Boolean,
    hideFurigana: Boolean,
    selectionHighlightLength: Int?,
    stabilizeForBackdrop: Boolean,
    modifier: Modifier,
    onProgressChanged: (Double) -> Unit,
    onProgressSaved: (Double) -> Unit,
    onTextSelected: (text: String, sentence: String, rect: ReaderSelectionRect?) -> Unit,
    onLinkActivated: (href: String) -> Unit,
    onTapOutside: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
) {
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

                layoutParams = ViewGroup.LayoutParams(
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
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val href = request.url?.toString().orEmpty()
                        if (!href.isInternalReaderHref()) return false
                        bridge.onLinkActivated(href)
                        return true
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
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
                            result?.toDoubleOrNull()?.let { bridge.onProgressChanged(it.coerceIn(0.0, 1.0)) }
                        }
                    }
                    saveProgressRunnable?.let(handler::removeCallbacks)
                    saveProgressRunnable = Runnable {
                        (view as WebView).evaluateJavascript("window.moriReader.calculateProgress()") { result ->
                            result?.toDoubleOrNull()?.let { bridge.onProgressSaved(it.coerceIn(0.0, 1.0)) }
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
            bridge.onTextSelected = { text, sentence, rect -> currentTextSelected.value(text, sentence, rect) }
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
            bridge.webView = webView
            val targetLayerType = if (stabilizeForBackdrop) {
                View.LAYER_TYPE_SOFTWARE
            } else {
                View.LAYER_TYPE_NONE
            }
            if (webView.layerType != targetLayerType) {
                webView.setLayerType(targetLayerType, null)
            }
            val sourceUrl = chapter?.sourceUrl
            val key = listOf(
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
            ).joinToString("|")
            if (sourceUrl != null && webView.tag != key) {
                webView.tag = key
                bridge.isRestoring = true
                bridge.appliedSelectionHighlightLength = null
                webView.alpha = 0f
                webView.loadUrl(sourceUrl)
            } else if (bridge.appliedSelectionHighlightLength != selectionHighlightLength) {
                bridge.appliedSelectionHighlightLength = selectionHighlightLength
                val script = selectionHighlightLength
                    ?.takeIf { it > 0 }
                    ?.let { "window.moriSelection && window.moriSelection.highlightSelection($it)" }
                    ?: "window.moriSelection && window.moriSelection.clearCustomHighlight()"
                webView.evaluateJavascript(script, null)
            }
        },
    )
}

private class MoriReaderWebView(context: Context) : WebView(context) {
    @Suppress("DEPRECATION")
    fun toJsViewportPoint(x: Float, y: Float): Pair<Float, Float> {
        val currentScale = scale.takeIf { it > 0f } ?: 1f
        return (x / currentScale) to (y / currentScale)
    }

    fun isAtContinuousBoundary(
        direction: String,
        verticalWriting: Boolean,
        threshold: Int,
    ): Boolean {
        val offset = if (verticalWriting) {
            computeHorizontalScrollOffset()
        } else {
            computeVerticalScrollOffset()
        }
        val range = if (verticalWriting) {
            computeHorizontalScrollRange()
        } else {
            computeVerticalScrollRange()
        }
        val extent = if (verticalWriting) {
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
            else -> false
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

    override fun onTouch(view: View, event: MotionEvent): Boolean {
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
    val script = """
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
        val direction = if (verticalWriting) {
            if (swipeLeft) "backward" else "forward"
        } else {
            if (swipeLeft) "forward" else "backward"
        }
        val atBoundary = webView.isAtContinuousBoundary(
            direction = direction,
            verticalWriting = verticalWriting,
            threshold = 24,
        )
        if (!atBoundary) return
        if (direction == "forward") onNextChapter() else onPreviousChapter()
        return
    }

    val direction = if (verticalWriting) {
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
        val rect = rectJson?.let {
            ReaderSelectionRect(
                x = it.optDouble("x").toFloat(),
                y = it.optDouble("y").toFloat(),
                width = it.optDouble("width").toFloat(),
                height = it.optDouble("height").toFloat(),
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
            webView?.animate()
                ?.alpha(1f)
                ?.setDuration(250L)
                ?.start()
        }
    }
}

private fun readerBootstrapScript(
    progress: Double,
    fragment: String?,
    verticalWriting: Boolean,
    isDark: Boolean,
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    characterSpacing: Double,
    continuousMode: Boolean,
    hideFurigana: Boolean,
): String {
    val writingMode = if (verticalWriting) "vertical-rl" else "horizontal-tb"
    val background = if (isDark) "#101010" else "#fbfaf7"
    val foreground = if (isDark) "#f2f2f2" else "#1b1b1b"
    val safeFontSize = fontSize.coerceIn(16, 40)
    val safeLineHeight = lineHeight.coerceIn(1.0, 2.5)
    val safeHorizontalPadding = horizontalPadding.coerceIn(0, 50).toDouble()
    val safeVerticalPadding = verticalPadding.coerceIn(0, 50).toDouble()
    val safeCharacterSpacing = characterSpacing.coerceIn(-10.0, 10.0)
    val bottomOverlap = if (verticalWriting) safeFontSize else 0
    val columnGap = if (verticalWriting) {
        "calc(${safeVerticalPadding}vh + ${bottomOverlap}px)"
    } else {
        "${safeHorizontalPadding}vw"
    }
    val bodyPadding = "${safeVerticalPadding / 2.0}vh ${safeHorizontalPadding / 2.0}vw"
    val bottomPaddingCss = if (verticalWriting && bottomOverlap > 0) {
        "padding-bottom: calc(${safeVerticalPadding / 2.0}vh + ${bottomOverlap}px) !important;"
    } else {
        ""
    }
    val pageBreakCss = if (avoidPageBreak) {
        """
            p {
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
        """.trimIndent()
    } else {
        ""
    }
    val textSpacingCss = """
            line-height: $safeLineHeight !important;
            letter-spacing: ${safeCharacterSpacing / 100.0}em !important;
        """.trimIndent()
    val gridCss = if (!justifyText) {
        """
            text-align: start !important;
            hanging-punctuation: allow-end !important;
            line-break: strict !important;
        """.trimIndent()
    } else {
        ""
    }
    val layoutCss = if (continuousMode) {
        val perpendicularOverflow = if (verticalWriting) {
            "overflow-y: hidden !important;"
        } else {
            "overflow-x: hidden !important;"
        }
        val imgWidth = if (verticalWriting) "none" else "${100 - safeHorizontalPadding}vw"
        val imgHeight = if (verticalWriting) {
            "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)"
        } else {
            "none"
        }
        """
            html, body {
              margin: 0 !important;
              padding: 0 !important;
              min-width: 100vw !important;
              min-height: 100vh !important;
              background: $background !important;
              color: $foreground !important;
              writing-mode: $writingMode !important;
              $perpendicularOverflow
            }
            body {
              box-sizing: border-box !important;
              font-family: serif !important;
              font-size: ${safeFontSize}px !important;
              $textSpacingCss
              padding: $bodyPadding !important;
              $bottomPaddingCss
              $gridCss
            }
            img.block-img {
              max-width: $imgWidth !important;
              max-height: $imgHeight !important;
              width: auto !important;
              height: auto !important;
              display: block !important;
              margin: auto !important;
              object-fit: contain !important;
            }
            svg {
              max-width: $imgWidth !important;
              max-height: $imgHeight !important;
              width: 100% !important;
              height: 100% !important;
              display: block !important;
              margin: auto !important;
            }
        """
    } else {
        """
            html, body {
              margin: 0 !important;
              padding: 0 !important;
              width: var(--page-width, 100vw) !important;
              height: var(--page-height, 100vh) !important;
              overflow: hidden !important;
              background: $background !important;
              color: $foreground !important;
              writing-mode: $writingMode !important;
            }
            body {
              box-sizing: border-box !important;
              font-family: serif !important;
              font-size: ${safeFontSize}px !important;
              $textSpacingCss
              column-width: var(--page-width, 100vw) !important;
              column-gap: $columnGap !important;
              padding: $bodyPadding !important;
              $bottomPaddingCss
              $gridCss
            }
            img.block-img {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
              width: auto !important;
              height: auto !important;
              display: block !important;
              margin: auto !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
              object-fit: contain !important;
            }
            svg {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
              width: 100% !important;
              height: 100% !important;
              display: block !important;
              margin: auto !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
        """
    }.trimIndent()
    val spacerJs = if (verticalWriting) {
        """
          var spacer = document.createElement('div');
          spacer.style.height = 'calc(${safeVerticalPadding / 2.0}vh + ${bottomOverlap}px)';
          spacer.style.width = '100%';
          spacer.style.display = 'block';
          spacer.style.breakInside = 'avoid';
          document.body.appendChild(spacer);
        """
    } else {
        """
          var spacer = document.createElement('div');
          spacer.style.height = '100%';
          spacer.style.width = '${safeHorizontalPadding / 2.0}vw';
          spacer.style.display = 'block';
          spacer.style.breakInside = 'avoid';
          document.body.appendChild(spacer);
        """
    }
    val pagingSpacerJs = if (continuousMode) "" else spacerJs
    val restore = if (fragment != null) {
        "window.moriReader.jumpToFragment(${fragment.jsString()});"
    } else {
        "window.moriReader.restoreProgress($progress);"
    }
    return """
        (function() {
          var viewport = document.querySelector('meta[name="viewport"]');
          if (viewport) viewport.remove();
          viewport = document.createElement('meta');
          viewport.name = 'viewport';
          viewport.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
          document.head.appendChild(viewport);

          var style = document.createElement('style');
          style.textContent = `
            $layoutCss
            figure {
              margin: 0 !important;
              padding: 0 !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
            $pageBreakCss
            body.mori-image-only img.block-img,
            body.mori-image-only svg {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
            }
            a {
              color: rgba(66, 108, 245, 1) !important;
            }
            rt, rp { user-select: none; }
            ::selection { background: rgba(120, 150, 255, 0.35); }
            ::highlight(mori-selection) {
              background-color: rgba(120, 150, 255, 0.35);
              color: inherit;
            }
          `;
          document.head.appendChild(style);
          document.documentElement.style.setProperty('--page-height', window.innerHeight + 'px');
          document.documentElement.style.setProperty('--page-width', window.innerWidth + 'px');
          $pagingSpacerJs

          if ($hideFurigana) {
            document.querySelectorAll('rt, rp').forEach(function(el) { el.remove(); });
          }

          document.querySelectorAll('ruby').forEach(function(ruby) {
            Array.from(ruby.childNodes).forEach(function(node) {
              if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
                var span = document.createElement('span');
                span.textContent = node.textContent;
                node.replaceWith(span);
              }
            });
          });

          document.addEventListener('click', function(event) {
            var anchor = event.target && event.target.closest ? event.target.closest('a[href]') : null;
            if (!anchor) return;
            var rawHref = anchor.getAttribute('href') || '';
            var trimmed = rawHref.trim();
            if (!trimmed) return;
            if (/^[a-z][a-z0-9+.-]*:/i.test(trimmed) && !trimmed.toLowerCase().startsWith('file:')) return;
            event.preventDefault();
            AndroidMoriReader.linkActivated(trimmed);
          }, true);

          var hasMatchableText = Array.from(document.body.querySelectorAll('*'))
            .filter(function(el) { return !el.closest('rt, rp, script, style'); })
            .some(function(el) {
              return Array.from(el.childNodes).some(function(node) {
                return node.nodeType === Node.TEXT_NODE &&
                  /[0-9A-Za-z○◯々-〇〻ぁ-ゖゝ-ゞァ-ヺー０-９Ａ-Ｚａ-ｚｦ-ﾝ\p{Radical}\p{Unified_Ideograph}]/u.test(node.textContent || '');
              });
            });
          var mediaCount = document.querySelectorAll('img, svg, image').length;
          if (!hasMatchableText && mediaCount > 0) {
            document.body.classList.add('mori-image-only');
          }

          document.querySelectorAll('svg').forEach(function(svg) {
            svg.classList.add('block-img');
          });

          var imagePromises = Array.from(document.querySelectorAll('img')).map(function(img) {
            return new Promise(function(resolve) {
              var mark = function() {
                var isGaiji = img.classList.contains('gaiji') || img.classList.contains('gaiji-line');
                var parentText = (img.parentElement && img.parentElement.textContent || '').trim();
                var standalone = document.body.classList.contains('mori-image-only') ||
                  parentText.length === 0 ||
                  !!img.closest('figure, .cover, .image, .illustration, .illust');
                var attrWidth = Number.parseInt(img.getAttribute('width') || '0', 10);
                var attrHeight = Number.parseInt(img.getAttribute('height') || '0', 10);
                var isLarge = img.naturalWidth > 256 || img.naturalHeight > 256 || attrWidth > 256 || attrHeight > 256;
                if (!isGaiji && (isLarge || standalone)) {
                  img.classList.add('block-img');
                }
                resolve();
              };
              if (img.complete && img.naturalWidth > 0) mark();
              else {
                img.onload = mark;
                img.onerror = resolve;
              }
            });
          });

          window.moriReader = {
            ttuRegexNegated: /[^0-9A-Za-z○◯々-〇〻ぁ-ゖゝ-ゞァ-ヺー０-９Ａ-Ｚａ-ｚｦ-ﾝ\p{Radical}\p{Unified_Ideograph}]+/gimu,
            pageHeight: window.innerHeight,
            pageWidth: window.innerWidth,
            continuousMode: $continuousMode,
            isVertical: function() {
              return window.getComputedStyle(document.body).writingMode === 'vertical-rl';
            },
            isFurigana: function(node) {
              var el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return !!(el && el.closest('rt, rp'));
            },
            normalizeText: function(text) {
              return (text || '').replace(this.ttuRegexNegated, '');
            },
            linkAtPoint: function(x, y) {
              var element = document.elementFromPoint(x, y);
              var anchor = element && element.closest ? element.closest('a[href]') : null;
              if (!anchor) return null;
              var rawHref = anchor.getAttribute('href') || '';
              var trimmed = rawHref.trim();
              if (!trimmed) return null;
              if (/^[a-z][a-z0-9+.-]*:/i.test(trimmed) && !trimmed.toLowerCase().startsWith('file:')) return null;
              return trimmed;
            },
            countChars: function(text) {
              return Array.from(this.normalizeText(text)).length;
            },
            createWalker: function(rootNode) {
              var root = rootNode || document.body;
              var self = this;
              return document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
                acceptNode: function(n) {
                  return self.isFurigana(n) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
                }
              });
            },
            getRect: function(target) {
              var rect = target.getClientRects()[0];
              return rect || target.getBoundingClientRect();
            },
            notifyRestoreComplete: function() {
              AndroidMoriReader.restoreCompleted();
            },
            getScrollContext: function() {
              var vertical = this.isVertical();
              var scrollEl = document.body;
              var pageSize = vertical ? this.pageHeight : this.pageWidth;
              var totalSize = vertical ? scrollEl.scrollHeight : scrollEl.scrollWidth;
              var maxScroll = Math.max(0, totalSize - pageSize);
              return { vertical: vertical, scrollEl: scrollEl, pageSize: pageSize, maxScroll: maxScroll };
            },
            setScrollOffset: function(context, scroll) {
              var clampedScroll = Math.min(Math.max(0, scroll), context.maxScroll);
              if (context.vertical) context.scrollEl.scrollTop = clampedScroll;
              else context.scrollEl.scrollLeft = clampedScroll;
              return clampedScroll;
            },
            alignToPage: function(context, anchor) {
              if (context.pageSize <= 0) return 0;
              var pageIndex = Math.floor(Math.max(0, anchor) / context.pageSize);
              return Math.min(Math.max(0, pageIndex * context.pageSize), context.maxScroll);
            },
            contentEndOffset: function(context) {
              var currentScroll = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
              var maxEnd = 0;
              var walker = this.createWalker();
              var node;

              while (node = walker.nextNode()) {
                if (this.countChars(node.textContent) <= 0) continue;
                var range = document.createRange();
                range.selectNodeContents(node);
                Array.from(range.getClientRects()).forEach(function(rect) {
                  if (rect.width <= 0 || rect.height <= 0) return;
                  var end = (context.vertical ? rect.bottom : rect.right) + currentScroll;
                  maxEnd = Math.max(maxEnd, end);
                });
              }

              document.querySelectorAll('img, svg, video, canvas').forEach(function(element) {
                var rect = element.getBoundingClientRect();
                if (rect.width <= 0 || rect.height <= 0) return;
                var end = (context.vertical ? rect.bottom : rect.right) + currentScroll;
                maxEnd = Math.max(maxEnd, end);
              });

              return maxEnd > 0 ? maxEnd : context.maxScroll + context.pageSize;
            },
            lastContentPageScroll: function(context) {
              if (context.pageSize <= 0) return 0;
              var contentEnd = Math.max(0, this.contentEndOffset(context) - 1);
              var lastPage = Math.floor(contentEnd / context.pageSize) * context.pageSize;
              return Math.min(Math.max(0, lastPage), context.maxScroll);
            },
            maxScroll: function() {
              if (this.isVertical()) return Math.max(0, document.body.scrollHeight - window.innerHeight);
              return Math.max(0, document.body.scrollWidth - window.innerWidth);
            },
            currentScroll: function() {
              return this.isVertical() ? document.body.scrollTop : document.body.scrollLeft;
            },
            setScroll: function(value) {
              var max = this.maxScroll();
              var target = Math.max(0, Math.min(max, value));
              if (this.isVertical()) document.body.scrollTop = target;
              else document.body.scrollLeft = target;
              return target;
            },
            scrollToContinuousStart: function() {
              if (this.isVertical()) {
                window.scrollTo(document.body.scrollWidth, 0);
              } else {
                window.scrollTo(0, 0);
              }
            },
            scrollToContinuousEnd: function() {
              if (this.isVertical()) {
                window.scrollTo(0, 0);
              } else {
                window.scrollTo(0, document.body.scrollHeight);
              }
            },
            registerSnapScroll: function(initialScroll) {
              if (window.snapScrollRegistered) return;
              window.snapScrollRegistered = true;
              window.lastPageScroll = initialScroll;
              var self = this;
              document.body.addEventListener('scroll', function() {
                if (window.programmaticPageScroll) return;
                var vertical = self.isVertical();
                var pageSize = vertical ? self.pageHeight : self.pageWidth;
                var current = vertical ? document.body.scrollTop : document.body.scrollLeft;
                var snapped = Math.round(current / pageSize) * pageSize;
                if (Math.abs(current - snapped) > 1) {
                  if (vertical) document.body.scrollTop = window.lastPageScroll;
                  else document.body.scrollLeft = window.lastPageScroll;
                } else {
                  window.lastPageScroll = snapped;
                }
              }, { passive: true });
            },
            calculateProgress: function() {
              var vertical = this.isVertical();
              var walker = this.createWalker();
              var totalChars = 0;
              var exploredChars = 0;
              var node;

              while (node = walker.nextNode()) {
                var nodeLen = this.countChars(node.textContent);
                totalChars += nodeLen;

                if (nodeLen > 0) {
                  var range = document.createRange();
                  range.selectNodeContents(node);
                  var rect = this.getRect(range);
                  var explored = this.continuousMode
                    ? (vertical ? rect.left > window.innerWidth : rect.bottom < 0)
                    : ((vertical ? rect.top : rect.left) < 0);
                  if (explored) {
                    exploredChars += nodeLen;
                  }
                }
              }

              return totalChars > 0 ? exploredChars / totalChars : 0;
            },
            restoreProgress: async function(progress) {
              await document.fonts.ready;
              if (this.continuousMode) {
                if (progress <= 0) {
                  this.scrollToContinuousStart();
                  requestAnimationFrame(() => this.scrollToContinuousStart());
                  this.notifyRestoreComplete();
                  return;
                }

                if (progress >= 0.99) {
                  this.scrollToContinuousEnd();
                  requestAnimationFrame(() => {
                    this.scrollToContinuousEnd();
                    requestAnimationFrame(() => this.notifyRestoreComplete());
                  });
                  return;
                }

                var vertical = this.isVertical();
                var walker = this.createWalker();
                var totalChars = 0;
                var node;

                while (node = walker.nextNode()) {
                  totalChars += this.countChars(node.textContent);
                }

                if (totalChars <= 0) {
                  this.notifyRestoreComplete();
                  return;
                }

                var targetCharCount = Math.ceil(totalChars * progress);
                var runningSum = 0;
                var targetNode = null;

                walker = this.createWalker();
                while (node = walker.nextNode()) {
                  runningSum += this.countChars(node.textContent);
                  targetNode = node;
                  if (runningSum > targetCharCount) break;
                }

                if (targetNode && targetNode.parentElement) {
                  targetNode.parentElement.scrollIntoView({
                    block: progress >= 0.999999 ? 'end' : 'start',
                    inline: 'nearest',
                    behavior: 'instant'
                  });
                }

                requestAnimationFrame(() => {
                  requestAnimationFrame(() => this.notifyRestoreComplete());
                });
                return;
              }
              var context = this.getScrollContext();

              if (context.pageSize <= 0) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              if (progress <= 0) {
                this.setScrollOffset(context, 0);
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              if (progress >= 0.99) {
                var lastPage = this.lastContentPageScroll(context);
                this.setScrollOffset(context, lastPage);
                requestAnimationFrame(() => {
                  this.setScrollOffset(context, lastPage);
                  this.registerSnapScroll(lastPage);
                  requestAnimationFrame(() => this.notifyRestoreComplete());
                });
                return;
              }

              var walker = this.createWalker();
              var totalChars = 0;
              var node;

              while (node = walker.nextNode()) {
                totalChars += this.countChars(node.textContent);
              }

              if (totalChars <= 0) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              var targetCharCount = Math.ceil(totalChars * progress);
              var runningSum = 0;
              var targetNode = null;

              walker = this.createWalker();
              while (node = walker.nextNode()) {
                runningSum += this.countChars(node.textContent);
                if (runningSum > targetCharCount) {
                  targetNode = node;
                  break;
                }
              }

              if (targetNode) {
                var range = document.createRange();
                range.setStart(targetNode, 0);
                range.setEnd(targetNode, 1);
                var rect = this.getRect(range);
                var anchor = (context.vertical ? rect.top : rect.left) + (context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft);
                var targetScroll = this.alignToPage(context, anchor);

                this.setScrollOffset(context, targetScroll);
                requestAnimationFrame(() => {
                  this.setScrollOffset(context, targetScroll);
                  this.registerSnapScroll(targetScroll);
                });
              } else {
                this.registerSnapScroll(0);
              }

              requestAnimationFrame(() => {
                requestAnimationFrame(() => this.notifyRestoreComplete());
              });
            },
            jumpToFragment: async function(fragment) {
              await document.fonts.ready;
              var rawFragment = (fragment || '').trim();
              var target = rawFragment && (document.getElementById(rawFragment) || document.getElementsByName(rawFragment)[0]);

              if (this.continuousMode) {
                if (!target) {
                  this.notifyRestoreComplete();
                  return false;
                }
                target.scrollIntoView();
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    AndroidMoriReader.progressSaved(this.calculateProgress());
                    this.notifyRestoreComplete();
                  });
                });
                return true;
              }

              var context = this.getScrollContext();
              if (context.pageSize <= 0 || !target) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return false;
              }

              var rect = this.getRect(target);
              var currentScroll = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
              var anchor = (context.vertical ? rect.top : rect.left) + currentScroll;
              var targetScroll = this.alignToPage(context, anchor);

              this.setScrollOffset(context, targetScroll);

              requestAnimationFrame(() => {
                this.setScrollOffset(context, targetScroll);
                this.registerSnapScroll(targetScroll);
                requestAnimationFrame(() => {
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  this.notifyRestoreComplete();
                });
              });

              return true;
            },
            continuousBoundary: function(direction) {
              if (!this.continuousMode) return 'none';
              var max = this.maxScroll();
              var current = this.currentScroll();
              if (direction === 'forward' && current >= max - 2) return 'forward';
              if (direction === 'backward' && current <= 2) return 'backward';
              return 'none';
            },
            paginate: function(direction) {
              var context = this.getScrollContext();
              var page = context.pageSize;
              if (page <= 0) return 'limit';

              if (direction === 'forward') {
                var maxAligned = this.lastContentPageScroll(context);
                var current = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
                if ((current + page) <= (maxAligned + 1)) {
                  var forwardTarget = Math.round((current + page) / page) * page;
                  window.lastPageScroll = forwardTarget;
                  this.setScrollOffset(context, forwardTarget);
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  return 'scrolled';
                }
                return 'limit';
              } else {
                var currentBack = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
                if (currentBack > 0) {
                  var backwardTarget = Math.round((currentBack - page) / page) * page;
                  window.lastPageScroll = backwardTarget;
                  this.setScrollOffset(context, backwardTarget);
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  return 'scrolled';
                }
                return 'limit';
              }
            }
          };

          window.moriSelection = {
            selection: null,
            scanDelimiters: '。、！？…‥「」『』（）()【】〈〉《》〔〕｛｝{}［］[]・：；:;，,.─\n\r',
            sentenceDelimiters: '。！？.!?\n\r',
            trailingSentenceChars: '。、！？…‥」』）)】〉》〕｝}］]',
            brackets: {'「':'」', '『':'』', '（':'）', '(' :')', '【':'】', '〈':'〉', '《':'》', '〔':'〕', '｛':'｝', '{':'}', '［':'］', '[':']'},
            isBoundary: function(ch) {
              return !ch || /^[\s\u3000]$/.test(ch) || this.scanDelimiters.indexOf(ch) >= 0;
            },
            isFurigana: function(node) {
              var el = node && node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return !!(el && el.closest('rt, rp'));
            },
            findTextScope: function(node) {
              var el = node && node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return (el && el.closest('p, li, blockquote, div, section, article')) || document.body;
            },
            createWalker: function(rootNode) {
              var self = this;
              return document.createTreeWalker(rootNode || document.body, NodeFilter.SHOW_TEXT, {
                acceptNode: function(node) {
                  return self.isFurigana(node) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
                }
              });
            },
            rangeContainsPoint: function(range, x, y) {
              var rects = range.getClientRects();
              for (var i = 0; i < rects.length; i++) {
                var rect = rects[i];
                if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) return true;
              }
              var fallback = range.getBoundingClientRect();
              return x >= fallback.left && x <= fallback.right && y >= fallback.top && y <= fallback.bottom;
            },
            caretRange: function(x, y) {
              if (document.caretPositionFromPoint) {
                var pos = document.caretPositionFromPoint(x, y);
                if (!pos) return null;
                var range = document.createRange();
                range.setStart(pos.offsetNode, pos.offset);
                range.collapse(true);
                return range;
              }
              if (document.caretRangeFromPoint) {
                var webkitRange = document.caretRangeFromPoint(x, y);
                if (webkitRange) return webkitRange;
              }
              var element = document.elementFromPoint(x, y);
              var scope = element ? this.findTextScope(element) : document.body;
              var walker = this.createWalker(scope);
              var probe = document.createRange();
              var node;
              while (node = walker.nextNode()) {
                var text = node.textContent || '';
                for (var i = 0; i < text.length; i++) {
                  probe.setStart(node, i);
                  probe.setEnd(node, i + 1);
                  if (this.rangeContainsPoint(probe, x, y)) {
                    probe.collapse(true);
                    return probe;
                  }
                }
              }
              return null;
            },
            characterAtPoint: function(x, y) {
              var range = this.caretRange(x, y);
              if (!range || range.startContainer.nodeType !== Node.TEXT_NODE) return null;
              var node = range.startContainer;
              if (this.isFurigana(node)) return null;
              var text = node.textContent || '';
              var caret = range.startOffset;
              var offsets = [caret, caret - 1, caret + 1];
              for (var i = 0; i < offsets.length; i++) {
                var offset = offsets[i];
                if (offset < 0 || offset >= text.length) continue;
                var charRange = document.createRange();
                charRange.setStart(node, offset);
                charRange.setEnd(node, offset + 1);
                if (this.rangeContainsPoint(charRange, x, y)) {
                  return this.isBoundary(text[offset]) ? null : { node: node, offset: offset };
                }
              }
              return null;
            },
            sentenceAt: function(node, offset) {
              var scope = this.findTextScope(node);
              var walker = this.createWalker(scope);
              var before = [];
              var current = node;
              var limit = offset;
              walker.currentNode = node;
              while (current) {
                var text = current.textContent || '';
                var foundStart = false;
                for (var i = limit - 1; i >= 0; i--) {
                  if (this.sentenceDelimiters.indexOf(text[i]) >= 0) {
                    before.push(text.slice(i + 1, limit));
                    foundStart = true;
                    break;
                  }
                }
                if (foundStart) break;
                before.push(text.slice(0, limit));
                current = walker.previousNode();
                if (current) limit = (current.textContent || '').length;
              }

              var after = [];
              current = node;
              var start = offset;
              walker.currentNode = node;
              while (current) {
                var content = current.textContent || '';
                var foundEnd = false;
                for (var j = start; j < content.length; j++) {
                  if (this.sentenceDelimiters.indexOf(content[j]) >= 0) {
                    var end = j + 1;
                    while (end < content.length && this.trailingSentenceChars.indexOf(content[end]) >= 0) end++;
                    after.push(content.slice(start, end));
                    foundEnd = true;
                    break;
                  }
                }
                if (foundEnd) break;
                after.push(content.slice(start));
                current = walker.nextNode();
                start = 0;
              }

              var sentence = (before.reverse().join('') + after.join('')).trim();
              var openBrackets = Object.keys(this.brackets);
              var closeBrackets = Object.values(this.brackets);
              var stack = [];
              var unmatchedClose = [];

              for (var index = 0; index < sentence.length; index++) {
                var ch = sentence[index];
                if (openBrackets.indexOf(ch) >= 0) {
                  stack.push(ch);
                } else if (closeBrackets.indexOf(ch) >= 0) {
                  if (stack.length > 0 && this.brackets[stack[stack.length - 1]] === ch) {
                    stack.pop();
                  } else {
                    unmatchedClose.push(ch);
                  }
                }
              }

              var startSlice = 0;
              while (stack.length > 0 && startSlice < sentence.length - 1) {
                if (stack[0] === sentence[startSlice]) {
                  stack.shift();
                } else {
                  break;
                }
                startSlice++;
              }

              var endSlice = sentence.length - 1;
              var endIndex = sentence.length - 1;
              while (unmatchedClose.length > 0 && endIndex > startSlice) {
                if (unmatchedClose[unmatchedClose.length - 1] === sentence[endIndex]) {
                  unmatchedClose.pop();
                  endSlice = endIndex - 1;
                } else if (this.sentenceDelimiters.indexOf(sentence[endIndex]) < 0) {
                  break;
                }
                endIndex--;
              }

              return sentence.slice(startSlice, endSlice + 1).trim();
            },
            clearSelection: function() {
              window.getSelection().removeAllRanges();
              this.clearCustomHighlight();
              this.selection = null;
            },
            clearCustomHighlight: function() {
              if (window.CSS && CSS.highlights) CSS.highlights.delete('mori-selection');
            },
            highlightSelection: function(charCount) {
              var ranges = (this.selection && this.selection.ranges) || [];
              window.getSelection().removeAllRanges();
              this.clearCustomHighlight();
              if (!ranges.length) return;
              if (!(window.CSS && CSS.highlights && window.Highlight)) return;
              var remaining = Math.max(0, charCount || 0);
              var highlights = [];
              for (var rIndex = 0; rIndex < ranges.length && remaining > 0; rIndex++) {
                var item = ranges[rIndex];
                var end = item.start;
                var content = item.node.textContent || '';
                while (end < item.end && remaining > 0) {
                  var codePoint = content.codePointAt(end);
                  if (codePoint === undefined) break;
                  end += String.fromCodePoint(codePoint).length;
                  remaining--;
                }
                if (end > item.start) {
                  var range = document.createRange();
                  range.setStart(item.node, item.start);
                  range.setEnd(item.node, end);
                  highlights.push(range);
                }
              }
              if (highlights.length) CSS.highlights.set('mori-selection', new Highlight(...highlights));
            },
            selectionRect: function(x, y) {
              if (!this.selection || !this.selection.ranges.length) return null;
              var first = this.selection.ranges[0];
              var range = document.createRange();
              range.setStart(first.node, first.start);
              range.setEnd(first.node, Math.min(first.end, first.start + 1));
              var rects = Array.from(range.getClientRects());
              var rect = rects.find(function(item) {
                return x >= item.left && x <= item.right && y >= item.top && y <= item.bottom;
              }) || range.getBoundingClientRect();
              return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
            },
            selectText: function(x, y, maxLength) {
              var hit = this.characterAtPoint(x, y);
              if (!hit) {
                this.clearSelection();
                return null;
              }
              if (this.selection && this.selection.node === hit.node && this.selection.offset === hit.offset) {
                this.clearSelection();
                return null;
              }
              this.clearSelection();
              var scope = this.findTextScope(hit.node);
              var walker = this.createWalker(scope);
              var node = hit.node;
              var offset = hit.offset;
              var selected = '';
              var ranges = [];
              walker.currentNode = node;

              while (node && selected.length < maxLength) {
                var text = node.textContent || '';
                var startOffset = offset;
                while (offset < text.length && selected.length < maxLength && !this.isBoundary(text[offset])) {
                  selected += text[offset];
                  offset++;
                }
                if (offset > startOffset) {
                  ranges.push({ node: node, start: startOffset, end: offset });
                }
                if (offset < text.length || selected.length >= maxLength) break;
                node = walker.nextNode();
                offset = 0;
              }
              if (!selected) {
                this.clearSelection();
                return null;
              }
              this.selection = {
                node: hit.node,
                offset: hit.offset,
                ranges: ranges,
                text: selected
              };
              AndroidMoriReader.textSelected(JSON.stringify({
                text: selected,
                sentence: this.sentenceAt(hit.node, hit.offset),
                rect: this.selectionRect(x, y)
              }));
              return selected;
            }
          };

          Promise.all(imagePromises).then(function() {
            return new Promise(function(resolve) { setTimeout(resolve, 50); });
          }).then(function() {
            $restore
          });
        })();
    """.trimIndent()
}

private fun WebView.hitTestIsInternalLink(): Boolean {
    val result = hitTestResult
    val href = result.extra?.trim().orEmpty()
    if (href.isBlank()) return false
    val isLink = when (result.type) {
        WebView.HitTestResult.SRC_ANCHOR_TYPE,
        WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> true
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
    val validScheme = scheme.withIndex().all { (index, char) ->
        if (index == 0) char.isLetter() else char.isLetterOrDigit() || char == '+' || char == '.' || char == '-'
    }
    return !validScheme || scheme.equals("file", ignoreCase = true)
}

private fun String.jsString(): String =
    buildString {
        append('\'')
        this@jsString.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
        append('\'')
    }
