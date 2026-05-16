package app.mori.reader.ui.pages.settings.anki

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDeck
import app.mori.reader.data.anki.AnkiField
import app.mori.reader.data.anki.AnkiHandlebar
import app.mori.reader.data.anki.AnkiNoteType
import app.mori.reader.data.anki.defaultAnkiHandlebarTokens
import app.mori.reader.data.anki.lapisFieldMappings
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.settings.presentation.DictionaryManagementState

internal data class AnkiDeckAndNoteTypeOptions(
    val decks: List<AnkiDeck>,
    val noteTypes: List<AnkiNoteType>,
)

internal fun AnkiState.deckAndNoteTypeOptions(): AnkiDeckAndNoteTypeOptions =
    AnkiDeckAndNoteTypeOptions(
        decks = deckOptions(),
        noteTypes = noteTypeOptions(),
    )

internal fun AnkiState.deckOptions(): List<AnkiDeck> =
    decks.ifEmpty {
        settings.selectedDeck
            ?.takeIf(String::isNotBlank)
            ?.let { listOf(AnkiDeck(id = it, name = it)) }
            .orEmpty()
    }

internal fun AnkiState.noteTypeOptions(): List<AnkiNoteType> =
    noteTypes.ifEmpty {
        settings.selectedNoteType
            ?.takeIf(String::isNotBlank)
            ?.let { listOf(AnkiNoteType(id = it, name = it)) }
            .orEmpty()
    }

internal fun AnkiState.selectedNoteType(): AnkiNoteType? = noteTypes.firstOrNull { it.name == settings.selectedNoteType }

internal fun AnkiState.editableFields(): List<AnkiField> {
    if (settings.selectedDeck.isNullOrBlank() && settings.selectedNoteType.isNullOrBlank()) {
        return lapisFieldMappings().keys.map(::AnkiField)
    }

    val fetchedFields = selectedNoteType()?.fields.orEmpty()
    if (fetchedFields.isNotEmpty()) return fetchedFields

    return emptyList()
}

internal fun AnkiState.effectiveFieldMappings(): Map<String, String> {
    val shouldUseLapisDefaults =
        settings.selectedDeck.isNullOrBlank() && settings.selectedNoteType.isNullOrBlank()
    val defaults = if (shouldUseLapisDefaults) lapisFieldMappings() else emptyMap()
    return defaults + settings.fieldMappings
}

@Composable
internal fun rememberAnkiHandlebars(dictionaryState: DictionaryManagementState): List<AnkiHandlebar> =
    remember(dictionaryState.termDictionaries) {
        defaultAnkiHandlebars() + dictionaryAnkiHandlebars(dictionaryState)
    }

internal fun defaultAnkiHandlebars(): List<AnkiHandlebar> = defaultAnkiHandlebarTokens().map(::AnkiHandlebar)

internal fun dictionaryAnkiHandlebars(dictionaryState: DictionaryManagementState): List<AnkiHandlebar> =
    dictionaryState
        .dictionaries(DictionaryType.Term)
        .filter { it.isEnabled }
        .map { AnkiHandlebar(token = "{single-glossary-${it.index.title}}") }

@Composable
internal fun AnkiConnectionMode.label(): String =
    when (this) {
        AnkiConnectionMode.AnkiDroid -> "AnkiDroid"
        AnkiConnectionMode.AnkiConnect -> "AnkiConnect"
    }
