package app.mori.reader.ui.pages.dictionary

import app.mori.reader.data.dictionary.DictionaryLookupEntry

internal data class DictionaryHtmlParams(
    val query: String,
    val entries: List<DictionaryLookupEntry>,
    val styles: Map<String, String>,
    val isSearching: Boolean,
    val hasSearched: Boolean,
    val errorMessage: String?,
    val searchingMessage: String,
    val noResultsMessage: String,
    val idleMessage: String,
    val playPronunciationLabel: String,
    val maxResults: Int,
    val scanLength: Int,
    val topPadding: Float,
    val collapseDictionaries: Boolean,
    val compactGlossaries: Boolean,
    val showExpressionTags: Boolean,
    val harmonicFrequency: Boolean,
    val deduplicatePitchAccents: Boolean,
    val isDark: Boolean,
    val eInkMode: Boolean,
    val audioSources: List<String>,
    val audioEnableAutoplay: Boolean,
    val audioPlaybackMode: String,
    val bottomPadding: Float,
    val edgeToEdgeContent: Boolean,
    val transparentBackground: Boolean,
    val eInkEntryBorderEnabled: Boolean,
    val enableInternalPopup: Boolean,
    val swipeDismissThreshold: Int,
    val ankiNeedsAudio: Boolean,
    val ankiAllowDuplicates: Boolean,
    val ankiUseAnkiConnect: Boolean,
    val ankiEmbedMedia: Boolean,
    val ankiCompactGlossaries: Boolean,
    val ankiDuplicateExpression: String?,
)

internal fun dictionaryHtml(params: DictionaryHtmlParams): String {
    val entriesJson = WebJson.encodeToString(params.entries)
    val stylesJson = WebJson.encodeToString(params.styles)
    val audioSourcesJson = WebJson.encodeToString(params.audioSources)
    val message =
        when {
            params.errorMessage != null -> params.errorMessage
            params.isSearching -> params.searchingMessage
            params.hasSearched && params.entries.isEmpty() -> params.noResultsMessage
            !params.hasSearched -> params.idleMessage
            else -> ""
        }
    return """
                    <!doctype html>
                    <html>
                    <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>${
        dictionaryCss(
            params.topPadding,
            params.bottomPadding,
            params.edgeToEdgeContent,
            params.transparentBackground,
            params.eInkEntryBorderEnabled,
        )
    }</style>
                    </head>
                    <body>
                    <div id="entries-container"></div>
                    <div id="empty-state"></div>
                    <div id="popup-backdrop" hidden></div>
                    <div id="popup" hidden><div id="popup-content"></div></div>
                    <script>
                    window.lookupEntries = $entriesJson;
                    window.lookupQuery = ${WebJson.encodeToString(params.query)};
                    window.dictionaryStyles = $stylesJson;
                    window.emptyMessage = ${WebJson.encodeToString(message)};
                    window.scanLength = ${params.scanLength};
                    window.maxResults = ${params.maxResults};
                    window.collapseDictionaries = ${params.collapseDictionaries};
                    window.compactGlossaries = ${params.compactGlossaries};
                    window.showExpressionTags = ${params.showExpressionTags};
                    window.harmonicFrequency = ${params.harmonicFrequency};
                    window.deduplicatePitchAccents = ${params.deduplicatePitchAccents};
                    window.isDark = ${WebJson.encodeToString(params.isDark)};
                    window.eInkMode = ${WebJson.encodeToString(params.eInkMode)};
                    window.audioSources = $audioSourcesJson;
                    window.audioEnableAutoplay = ${params.audioEnableAutoplay};
                    window.audioPlaybackMode = ${WebJson.encodeToString(params.audioPlaybackMode)};
                    window.enableInternalPopup = ${params.enableInternalPopup};
                    window.swipeThreshold = ${params.swipeDismissThreshold.coerceIn(0, 80)};
                    window.needsAudio = ${params.ankiNeedsAudio};
                    window.allowDupes = ${params.ankiAllowDuplicates};
                    window.useAnkiConnect = ${params.ankiUseAnkiConnect};
                    window.embedMedia = ${params.ankiEmbedMedia};
                    window.compactGlossariesAnki = ${params.ankiCompactGlossaries};
                    window.ankiDuplicateExpression = ${WebJson.encodeToString(params.ankiDuplicateExpression)};
                    document.documentElement.classList.add(window.isDark ? 'dark' : 'light');
                    if (window.eInkMode) document.documentElement.classList.add('eink');
                    </script>
                    <script>${dictionaryJs(params.playPronunciationLabel)}</script>
                    </body>
                    </html>
        """.trimIndent()
}

private fun dictionaryCss(
    topPadding: Float,
    bottomPadding: Float,
    edgeToEdgeContent: Boolean,
    transparentBackground: Boolean,
    eInkEntryBorderEnabled: Boolean,
): String {
    val containerPadding = if (edgeToEdgeContent) "0" else "0 2px"
    val pageBackground =
        if (transparentBackground || !edgeToEdgeContent) {
            "background: transparent;"
        } else {
            "background: var(--mori-entry-bg);"
        }
    val entryBackground =
        if (edgeToEdgeContent || transparentBackground) "transparent" else "var(--mori-entry-bg)"
    val entryRadius = if (edgeToEdgeContent) "0" else "8px"
    val eInkEntryBorder =
        if (eInkEntryBorderEnabled) {
            """
            :root.eink .entry {
                border: 2px solid var(--mori-hr-border);
                border-radius: 0;
                margin-bottom: 10px;
            }
            """.trimIndent()
        } else {
            """
            :root.eink .entry {
                margin-bottom: 0;
            }
            """.trimIndent()
        }
    val lastEntryMargin =
        if (edgeToEdgeContent) {
            """
            #entries-container > .entry:last-child { margin-bottom: 0; }
            #entries-container > hr:last-child { display: none; }
            """.trimIndent()
        } else {
            ""
        }
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
        :root.eink {
            --mori-text: #000000;
            --mori-text-secondary: #000000;
            --mori-entry-bg: #ffffff;
            --mori-audio-button-bg: #ffffff;
            --mori-audio-button-color: #000000;
            --mori-tag-bg: #ffffff;
            --mori-pitch-dict-bg: #000000;
            --mori-pitch-dict-text: #ffffff;
            --mori-freq-border: #000000;
            --mori-freq-dict-bg: #000000;
            --mori-freq-dict-text: #ffffff;
            --mori-summary-color: #000000;
            --mori-hr-border: #000000;
            --mori-link-color: #000000;
            --mori-popup-bg: #ffffff;
            --mori-popup-shadow: none;
            --mori-selection-bg: rgba(0, 0, 0, 0.18);
            --mori-image-overlay: rgba(0,0,0,0.9);
        }
        :root.dark.eink {
            --mori-text: #ffffff;
            --mori-text-secondary: #ffffff;
            --mori-entry-bg: #000000;
            --mori-audio-button-bg: #000000;
            --mori-audio-button-color: #ffffff;
            --mori-tag-bg: #000000;
            --mori-pitch-dict-bg: #ffffff;
            --mori-pitch-dict-text: #000000;
            --mori-freq-border: #ffffff;
            --mori-freq-dict-bg: #ffffff;
            --mori-freq-dict-text: #000000;
            --mori-summary-color: #ffffff;
            --mori-hr-border: #ffffff;
            --mori-link-color: #ffffff;
            --mori-popup-bg: #000000;
            --mori-popup-shadow: none;
            --mori-selection-bg: rgba(255, 255, 255, 0.22);
            --mori-image-overlay: rgba(255,255,255,0.9);
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
        .audio-button { width: 32px; height: 32px; border: none; border-radius: 16px; background: var(--mori-audio-button-bg); color: var(--mori-audio-button-color); font-size: 18px; line-height: 32px; padding: 0; }
        .mine-button { width: 32px; height: 32px; border: none; border-radius: 16px; background: var(--mori-tag-bg); color: var(--mori-text); font-size: 20px; line-height: 32px; padding: 0; }
        .mine-button.duplicate { background: var(--mori-audio-button-bg); color: var(--mori-audio-button-color); }
        .mine-button:disabled { opacity: 0.56; }
        .expression { font-size: 26px; line-height: 1.25; font-weight: 650; overflow-wrap: anywhere; }
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
        :root.eink .audio-button,
        :root.eink .mine-button,
        :root.eink .expr-tag,
        :root.eink .deinflection-tag,
        :root.eink .frequency-group,
        :root.eink #popup {
            border: 1px solid var(--mori-hr-border);
        }
        $eInkEntryBorder
        :root.eink #popup {
            border-radius: 0;
        }
        :root.eink hr {
            display: none;
        }
        ::highlight(hoshi-selection) { background: var(--mori-selection-bg); }
        """.trimIndent()
}
