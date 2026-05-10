package app.mori.reader.features.anki.presentation

import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.anki.AnkiMiningContext

sealed interface AnkiIntent {
    data object Connect : AnkiIntent

    data object CheckAvailability : AnkiIntent

    data object FetchDecksAndModels : AnkiIntent

    data class SelectDeck(
        val deckName: String?,
    ) : AnkiIntent

    data class SelectNoteType(
        val noteTypeName: String?,
    ) : AnkiIntent

    data class SetFieldMapping(
        val fieldName: String,
        val template: String,
    ) : AnkiIntent

    data class SetTags(
        val tags: List<String>,
    ) : AnkiIntent

    data class SetConnectionMode(
        val mode: AnkiConnectionMode,
    ) : AnkiIntent

    data class SetAnkiConnectUrl(
        val url: String,
    ) : AnkiIntent

    data class SetAnkiConnectTimeoutMillis(
        val timeoutMillis: Int,
    ) : AnkiIntent

    data class SetDuplicateScope(
        val scope: AnkiDuplicateScope,
    ) : AnkiIntent

    data class SetCheckAllModels(
        val enabled: Boolean,
    ) : AnkiIntent

    data class SetForceSync(
        val enabled: Boolean,
    ) : AnkiIntent

    data class SetAllowDuplicates(
        val enabled: Boolean,
    ) : AnkiIntent

    data class SetCompactGlossaries(
        val enabled: Boolean,
    ) : AnkiIntent

    data class SetEmbedMedia(
        val enabled: Boolean,
    ) : AnkiIntent

    data class SetShowLapisTemplateHint(
        val show: Boolean,
    ) : AnkiIntent

    data class MineNote(
        val content: AnkiMiningContent,
        val context: AnkiMiningContext,
    ) : AnkiIntent

    data class CheckDuplicate(
        val expression: String,
    ) : AnkiIntent

    data object DismissError : AnkiIntent
}
