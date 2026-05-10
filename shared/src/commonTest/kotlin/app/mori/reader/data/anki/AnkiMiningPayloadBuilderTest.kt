package app.mori.reader.data.anki

import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.book.ReaderBook
import app.mori.reader.data.book.ReaderChapter
import app.mori.reader.data.dictionary.DictionaryFrequency
import app.mori.reader.data.dictionary.DictionaryFrequencyGroup
import app.mori.reader.data.dictionary.DictionaryGlossary
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.dictionary.DictionaryPitchGroup
import kotlin.test.Test
import kotlin.test.assertEquals

class AnkiMiningPayloadBuilderTest {
    @Test
    fun buildsMiningContentFromDictionaryEntry() {
        val content =
            buildAnkiMiningContent(
                entry =
                    DictionaryLookupEntry(
                        expression = "日本語",
                        reading = "にほんご",
                        matched = "日本語",
                        glossaries =
                            listOf(
                                DictionaryGlossary(
                                    dictionary = "JMdict",
                                    content = """["Japanese language","Japanese"]""",
                                    definitionTags = "",
                                    termTags = "n",
                                ),
                                DictionaryGlossary(
                                    dictionary = "Pitch",
                                    content = "language",
                                    definitionTags = "",
                                    termTags = "",
                                ),
                            ),
                        frequencies =
                            listOf(
                                DictionaryFrequencyGroup(
                                    dictionary = "freq",
                                    frequencies = listOf(DictionaryFrequency(value = 100, displayValue = "100")),
                                ),
                            ),
                        pitches = listOf(DictionaryPitchGroup(dictionary = "pitch", pitchPositions = listOf(2))),
                    ),
                popupSelectionText = "selected text",
            )

        assertEquals("日本語", content.expression)
        assertEquals("にほんご", content.reading)
        assertEquals("日本語", content.matched)
        assertEquals("日本語[にほんご]", content.furiganaPlain)
        assertEquals("selected text", content.popupSelectionText)
        assertEquals(
            listOf(
                """JMdict: <div class="glossary-content">Japanese language; Japanese</div>""",
                """Pitch: <div class="glossary-content">language</div>""",
            ),
            content.glossaries.map { "${it.dictionaryTitle}: ${it.text}" },
        )
        assertEquals(
            listOf(
                """<span class="frequency-group" title="freq"><span class="frequency-dict-label">freq</span><span class="frequency-values">100</span></span>""",
            ),
            content.frequencies,
        )
        assertEquals("100", content.frequencyHarmonicRank)
        assertEquals(listOf("2"), content.pitchAccentPositions)
        assertEquals(listOf("nakadaka"), content.pitchAccentCategories)
    }

    @Test
    fun buildsReaderMiningContextFromReaderBookAndLookup() {
        val book =
            ReaderBook(
                info =
                    BookInfo(
                        id = "book",
                        title = "Kokoro",
                        coverPath = "content://cover",
                        importedAt = 1L,
                    ),
                chapters =
                    listOf(
                        ReaderChapter(
                            id = "c1",
                            title = "Sensei",
                            href = "chapter.xhtml",
                            sourceUrl = "",
                            index = 0,
                            characterStart = 0,
                            characterCount = 100,
                        ),
                    ),
                tableOfContents = emptyList(),
                totalCharacterCount = 100,
            )

        val context =
            buildReaderAnkiMiningContext(
                book = book,
                sentence = "私は先生を見た。",
                sasayakiCueId = "cue-1",
            )

        assertEquals("私は先生を見た。", context.sentence)
        assertEquals("Kokoro", context.documentTitle)
        assertEquals("content://cover", context.coverUri)
        assertEquals("mori_sasayaki_cue-1.mp3", context.sasayakiAudioFileName)
    }
}
