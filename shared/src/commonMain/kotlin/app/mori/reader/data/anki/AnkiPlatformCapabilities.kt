package app.mori.reader.data.anki

data class AnkiPlatformCapabilities(
    val availableModes: List<AnkiConnectionMode>,
    val preferredMode: AnkiConnectionMode,
)

expect fun ankiPlatformCapabilities(): AnkiPlatformCapabilities
