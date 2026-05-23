package app.mori.reader.features.settings.domain

import app.mori.reader.data.settings.ReaderSettings
import app.mori.reader.data.settings.ReaderThemeMode

fun shouldRefreshReaderLayout(
    previous: ReaderSettings,
    next: ReaderSettings,
    previousThemeMode: ReaderThemeMode,
    nextThemeMode: ReaderThemeMode,
): Boolean =
    previous.fontSize != next.fontSize ||
        previous.lineHeight != next.lineHeight ||
        previous.horizontalPadding != next.horizontalPadding ||
        previous.verticalPadding != next.verticalPadding ||
        previous.avoidPageBreak != next.avoidPageBreak ||
        previous.justifyText != next.justifyText ||
        previous.layoutAdvanced != next.layoutAdvanced ||
        previous.characterSpacing != next.characterSpacing ||
        previous.continuousMode != next.continuousMode ||
        previous.hideFurigana != next.hideFurigana ||
        previousThemeMode != nextThemeMode
