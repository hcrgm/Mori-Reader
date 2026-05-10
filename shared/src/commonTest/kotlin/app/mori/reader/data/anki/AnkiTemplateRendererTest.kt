package app.mori.reader.data.anki

import kotlin.test.Test
import kotlin.test.assertEquals

class AnkiTemplateRendererTest {
    @Test
    fun renderBoldsExpressionInsideSentence() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Sentence" to "{sentence}"),
                content = content(expression = "猫", matched = "黒い猫"),
                context = AnkiMiningContext(sentence = "黒い猫がいる。猫もいる。"),
            )

        assertEquals("<b>黒い猫</b>がいる。猫もいる。", rendered.fields.getValue("Sentence"))
    }

    @Test
    fun renderUsesDictionarySpecificGlossary() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Meaning" to "{single-glossary-大辞泉}"),
                content =
                    content(
                        glossaries =
                            listOf(
                                AnkiGlossary(dictionaryTitle = "JMdict", text = "cat"),
                                AnkiGlossary(dictionaryTitle = "大辞泉", text = "ねこ科の動物"),
                            ),
                    ),
                context = AnkiMiningContext(),
            )

        assertEquals("ねこ科の動物", rendered.fields.getValue("Meaning"))
    }

    @Test
    fun renderSelectedGlossaryUsesSelectedDictionaryLikeHoshi() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Meaning" to "{selected-glossary}"),
                content =
                    content(
                        selectedDictionary = "大辞泉",
                        glossaries =
                            listOf(
                                AnkiGlossary(dictionaryTitle = "JMdict", text = "first"),
                                AnkiGlossary(dictionaryTitle = "大辞泉", text = "second"),
                            ),
                    ),
                context = AnkiMiningContext(),
            )

        assertEquals("second", rendered.fields.getValue("Meaning"))
    }

    @Test
    fun renderKeepsUnmappedFieldsEmpty() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Expression" to "{expression}", "Empty" to ""),
                content = content(expression = "読む"),
                context = AnkiMiningContext(),
            )

        assertEquals("読む", rendered.fields.getValue("Expression"))
        assertEquals("", rendered.fields.getValue("Empty"))
    }

    @Test
    fun renderAudioOnlyFieldsExposeAudioMarkup() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Audio" to "{audio}"),
                content =
                    content(
                        audio =
                            listOf(
                                DictionaryMedia(
                                    fileName = "neko.mp3",
                                    mimeType = "audio/mpeg",
                                    dataBase64 = null,
                                ),
                            ),
                    ),
                context = AnkiMiningContext(),
            )

        assertEquals("[sound:neko.mp3]", rendered.fields.getValue("Audio"))
    }

    @Test
    fun renderMissingMediaPlaceholdersAsEmptyStrings() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping = mapOf("Media" to "{book-cover}{sasayaki-audio}{audio}"),
                content = content(audio = emptyList()),
                context = AnkiMiningContext(coverUri = null, sasayakiAudioFileName = null),
            )

        assertEquals("", rendered.fields.getValue("Media"))
    }

    @Test
    fun renderPitchAndCoverFieldsMatchHoshiFormatting() {
        val rendered =
            AnkiTemplateRenderer.render(
                mapping =
                    mapOf(
                        "Pitch" to "{pitch-accent-positions}",
                        "PitchCategories" to "{pitch-accent-categories}",
                        "Cover" to "{book-cover}",
                    ),
                content =
                    content(
                        pitchAccentPositions = listOf("0", "2"),
                        pitchAccentCategories = listOf("heiban", "nakadaka"),
                    ),
                context = AnkiMiningContext(coverUri = "content://cover"),
            )

        assertEquals(
            """<ol><li><span style="display:inline;"><span>[</span><span>0</span><span>]</span></span></li><li><span style="display:inline;"><span>[</span><span>2</span><span>]</span></span></li></ol>""",
            rendered.fields.getValue("Pitch"),
        )
        assertEquals("heiban,nakadaka", rendered.fields.getValue("PitchCategories"))
        assertEquals("content://cover", rendered.fields.getValue("Cover"))
    }

    private fun content(
        expression: String = "猫",
        reading: String = "ねこ",
        matched: String = expression,
        selectedGlossary: String? = "selected",
        selectedDictionary: String = "",
        glossaries: List<AnkiGlossary> = listOf(AnkiGlossary("JMdict", "cat")),
        audio: List<DictionaryMedia> = emptyList(),
        pitchAccentPositions: List<String> = emptyList(),
        pitchAccentCategories: List<String> = emptyList(),
    ): AnkiMiningContent =
        AnkiMiningContent(
            expression = expression,
            reading = reading,
            matched = matched,
            furiganaPlain = reading,
            glossaries = glossaries,
            selectedGlossary = selectedGlossary,
            selectedDictionary = selectedDictionary,
            audio = audio,
            pitchAccentPositions = pitchAccentPositions,
            pitchAccentCategories = pitchAccentCategories,
        )
}
