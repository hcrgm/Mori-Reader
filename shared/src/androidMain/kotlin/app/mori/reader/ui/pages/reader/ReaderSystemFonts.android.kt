package app.mori.reader.ui.pages.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File

@Composable
actual fun rememberReaderSystemFonts(): List<ReaderSystemFont> =
    remember {
        readAndroidSystemFonts()
    }

private fun readAndroidSystemFonts(): List<ReaderSystemFont> {
    val names =
        listOf(
            "/system/etc/fonts.xml",
            "/product/etc/fonts_customization.xml",
            "/system/etc/system_fonts.xml",
            "/vendor/etc/fonts.xml",
        ).flatMap { path -> readFontFamilies(File(path)) }

    return names
        .asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
        .map { ReaderSystemFont(family = it) }
        .toList()
        .ifEmpty {
            listOf(
                ReaderSystemFont("sans-serif"),
                ReaderSystemFont("serif"),
                ReaderSystemFont("monospace"),
            )
        }
}

private fun readFontFamilies(file: File): List<String> {
    if (!file.canRead()) return emptyList()
    return runCatching {
        file.inputStream().use { input ->
            val parser =
                XmlPullParserFactory
                    .newInstance()
                    .newPullParser()
                    .apply { setInput(input, null) }
            val names = mutableListOf<String>()
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "family") {
                    parser.getAttributeValue(null, "name")?.let(names::add)
                }
                event = parser.next()
            }
            names
        }
    }.getOrDefault(emptyList())
}
