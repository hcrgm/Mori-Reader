package app.mori.reader.data.anki

object AnkiTemplateRenderer {
    private val singleGlossaryPattern = Regex("""\{single-glossary-([^}]+)\}""")
    private val handlebarPattern = Regex("""\{.*?\}""")

    fun render(
        mapping: Map<String, String>,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ): AnkiRenderedNote =
        AnkiRenderedNote(
            fields = mapping.mapValues { (_, template) -> renderTemplate(template, content, context) },
        )

    private fun renderTemplate(
        template: String,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ): String {
        if (template.isEmpty()) return ""
        val withDynamicGlossaries =
            singleGlossaryPattern.replace(template) { match ->
                val dictionaryTitle = match.groupValues[1]
                content.singleGlossaries[dictionaryTitle]
                    ?: content.glossaries
                        .firstOrNull { it.dictionaryTitle == dictionaryTitle }
                        ?.text
                        .orEmpty()
            }
        val replacements = replacements(content, context)
        return handlebarPattern.replace(withDynamicGlossaries) { match ->
            replacements[match.value].orEmpty()
        }
    }

    private fun replacements(
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ): Map<String, String> {
        val firstGlossary =
            content.glossaryFirst.ifBlank {
                content.glossaries
                    .firstOrNull()
                    ?.text
                    .orEmpty()
            }
        val selectedGlossary =
            content.singleGlossaries[content.selectedDictionary]
                ?: content.glossaries
                    .firstOrNull { it.dictionaryTitle == content.selectedDictionary }
                    ?.text
                ?: content.selectedGlossary.orEmpty()
        val glossary =
            content.glossary.ifBlank {
                content.glossaries.joinToString("") { it.text }
            }
        val frequencies =
            content.frequenciesHtml.ifBlank {
                content.frequencies.joinToString("")
            }
        val frequencyHarmonicRank =
            content.freqHarmonicRank.ifBlank {
                content.frequencyHarmonicRank
            }
        val pitchPositions =
            content.pitchPositions.ifBlank {
                content.pitchAccentPositions.toPitchPositionHtml()
            }
        val pitchCategories =
            content.pitchCategories.ifBlank {
                content.pitchAccentCategories.joinToString(",")
            }
        return mapOf(
            AnkiHandlebarTokens.EXPRESSION to content.expression,
            AnkiHandlebarTokens.READING to content.reading,
            AnkiHandlebarTokens.FURIGANA_PLAIN to content.furiganaPlain,
            AnkiHandlebarTokens.AUDIO to content.audio,
            AnkiHandlebarTokens.GLOSSARY to glossary,
            AnkiHandlebarTokens.GLOSSARY_FIRST to firstGlossary,
            AnkiHandlebarTokens.SELECTED_GLOSSARY to selectedGlossary,
            AnkiHandlebarTokens.POPUP_SELECTION_TEXT to content.popupSelectionText,
            AnkiHandlebarTokens.SENTENCE to context.sentence.boldFirst(content.matched.ifBlank { content.expression }),
            AnkiHandlebarTokens.FREQUENCIES to frequencies,
            AnkiHandlebarTokens.FREQUENCY_HARMONIC_RANK to frequencyHarmonicRank,
            AnkiHandlebarTokens.PITCH_ACCENT_POSITIONS to pitchPositions,
            AnkiHandlebarTokens.PITCH_ACCENT_CATEGORIES to pitchCategories,
            AnkiHandlebarTokens.DOCUMENT_TITLE to context.documentTitle,
            AnkiHandlebarTokens.BOOK_COVER to context.coverUri.orEmpty(),
            AnkiHandlebarTokens.SASAYAKI_AUDIO to context.sasayakiAudioFileName?.let { "[sound:$it]" }.orEmpty(),
        )
    }

    private fun List<String>.toPitchPositionHtml(): String {
        if (isEmpty()) return ""
        return joinToString(
            prefix = "<ol>",
            postfix = "</ol>",
            separator = "",
        ) { position ->
            """<li><span style="display:inline;"><span>[</span><span>$position</span><span>]</span></span></li>"""
        }
    }

    private fun String.boldFirst(term: String): String {
        if (isBlank() || term.isBlank()) return this
        val index = indexOf(term)
        if (index == -1) return this
        return replaceRange(index, index + term.length, "<b>$term</b>")
    }
}
