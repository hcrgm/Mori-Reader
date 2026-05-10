package app.mori.reader.data.anki

class AnkiService(
    private val settingsRepository: AnkiSettingsRepository,
    transports: List<AnkiTransport>,
    private val shareFallback: AnkiShareFallback,
) {
    private val transportsByMode = transports.associateBy(AnkiTransport::mode)

    suspend fun connect(settings: AnkiSettings): Boolean = requireAvailable(settings)

    suspend fun fetchDecksAndModels(settings: AnkiSettings): AnkiFetchResult {
        val transport = requireAvailableTransport(settings)
        val result = transport.fetchDecksAndModels(settings)
        reconcileSelections(settings, result)
        return result
    }

    suspend fun mineNote(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        val transport = requireAvailableTransport(settings)
        try {
            transport.addNote(settings, content, context)
        } catch (exception: AnkiLocalApiUnavailableException) {
            if (settings.connectionMode != AnkiConnectionMode.AnkiDroid) {
                throw exception
            }
            shareFallback.share(settings, content, context)
            transport.recordAddedExpression(content.expression)
            return
        }
        if (settings.forceSync) {
            transport.sync(settings)
        }
    }

    suspend fun checkDuplicate(
        settings: AnkiSettings,
        expression: String,
    ): Boolean = requireAvailableTransport(settings).checkDuplicate(settings, expression)

    private suspend fun reconcileSelections(
        settings: AnkiSettings,
        result: AnkiFetchResult,
    ) {
        val deckName =
            settings.selectedDeck?.takeIf { selected -> result.decks.any { it.name == selected } }
                ?: result.decks.firstOrNull()?.name
                ?: settings.selectedDeck
        val noteType =
            settings.selectedNoteType?.takeIf { selected -> result.noteTypes.any { it.name == selected } }
                ?: result.noteTypes.firstOrNull()?.name
                ?: settings.selectedNoteType
        val fields =
            result.noteTypes
                .firstOrNull { it.name == noteType }
                ?.fields
                .orEmpty()
        val lapisMappings =
            result.noteTypes
                .firstOrNull { it.name == noteType }
                ?.takeIf(AnkiNoteType::isLapis)
                ?.let { lapisFieldMappings() }
                ?: noteType
                    ?.takeIf { it.contains("lapis", ignoreCase = true) }
                    ?.let { lapisFieldMappings() }
                ?: emptyMap()
        settingsRepository.updateSettings {
            it.copy(
                selectedDeck = deckName,
                selectedNoteType = noteType,
                cachedDecks = result.decks,
                cachedNoteTypes = result.noteTypes,
                fieldMappings =
                    if (fields.isEmpty() && lapisMappings.isEmpty()) {
                        it.fieldMappings
                    } else if (fields.isEmpty()) {
                        lapisMappings + it.fieldMappings.filterValues(String::isNotBlank)
                    } else {
                        fields.associate { field ->
                            field.name to
                                it.fieldMappings[field.name].orEmpty().ifBlank {
                                    lapisMappings[field.name].orEmpty()
                                }
                        }
                    },
            )
        }
    }

    private fun transportFor(settings: AnkiSettings): AnkiTransport =
        transportsByMode[settings.connectionMode]
            ?: error("Missing Anki transport for mode ${settings.connectionMode}")

    private suspend fun requireAvailable(settings: AnkiSettings): Boolean = transportFor(settings).ping(settings)

    private suspend fun requireAvailableTransport(settings: AnkiSettings): AnkiTransport = transportFor(settings).also { it.ping(settings) }
}

interface AnkiShareFallback {
    suspend fun share(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    )
}
