package app.mori.reader.data.anki

actual fun ankiPlatformCapabilities(): AnkiPlatformCapabilities =
    AnkiPlatformCapabilities(
        availableModes = listOf(AnkiConnectionMode.AnkiDroid, AnkiConnectionMode.AnkiConnect),
        preferredMode = AnkiConnectionMode.AnkiDroid,
    )
