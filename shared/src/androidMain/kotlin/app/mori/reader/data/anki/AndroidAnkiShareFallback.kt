package app.mori.reader.data.anki

import android.content.Context
import android.content.Intent

class AndroidAnkiShareFallback(
    context: Context,
) : AnkiShareFallback {
    private val appContext = context.applicationContext

    override suspend fun share(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        val rendered = AnkiTemplateRenderer.render(settings.fieldMappings, content, context).fields
        val firstField = rendered.values.firstOrNull { it.isNotBlank() } ?: content.expression
        val backText =
            rendered.values
                .drop(1)
                .filter(String::isNotBlank)
                .joinToString(separator = "\n\n")
                .ifBlank {
                    content.selectedGlossary ?: content.glossaries
                        .firstOrNull()
                        ?.text
                        .orEmpty()
                }
        val intent =
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, firstField)
                .putExtra(Intent.EXTRA_TEXT, backText)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(appContext.packageManager) != null) {
            appContext.startActivity(intent)
        } else {
            throw IllegalStateException("AnkiDroid is unavailable and no share target was found")
        }
    }
}
