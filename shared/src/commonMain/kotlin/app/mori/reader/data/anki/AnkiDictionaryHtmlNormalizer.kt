package app.mori.reader.data.anki

internal fun String.normalizeAnkiDictionaryHtml(): String {
    if (!contains("data-sc-img") || !contains("gloss-image")) return this
    if (contains(ANKI_GAIJI_IMAGE_STYLE_MARKER)) return this
    return this + ANKI_GAIJI_IMAGE_STYLE
}

private const val ANKI_GAIJI_IMAGE_STYLE_MARKER =
    ".yomitan-glossary [data-sc-img][data-sc-class=\"gaiji\"] .gloss-image-container"

private const val ANKI_GAIJI_IMAGE_STYLE =
    """<style>.yomitan-glossary [data-sc-img][data-sc-class="gaiji"]{display:inline!important;white-space:nowrap!important;vertical-align:baseline!important}.yomitan-glossary [data-sc-img][data-sc-class="gaiji"] .gloss-image-link{display:inline-block!important;vertical-align:text-bottom!important;max-width:1.2em!important}.yomitan-glossary [data-sc-img][data-sc-class="gaiji"] .gloss-image-container{display:inline-block!important;width:1em!important;height:1em!important;max-width:1em!important;max-height:1em!important;vertical-align:text-bottom!important;font-size:1em!important}.yomitan-glossary [data-sc-img][data-sc-class="gaiji"] .gloss-image-sizer{display:none!important}.yomitan-glossary [data-sc-img][data-sc-class="gaiji"] .gloss-image{position:static!important;width:1em!important;height:1em!important;vertical-align:text-bottom!important}</style>"""
