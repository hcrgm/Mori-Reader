package app.mori.reader.features.settings.domain

import app.mori.reader.data.settings.AppSettings

fun shouldRefreshReaderLayout(
    previous: AppSettings,
    next: AppSettings,
): Boolean =
    previous.reader.fontSize != next.reader.fontSize ||
        previous.reader.lineHeight != next.reader.lineHeight ||
        previous.reader.horizontalPadding != next.reader.horizontalPadding ||
        previous.reader.verticalPadding != next.reader.verticalPadding ||
        previous.reader.avoidPageBreak != next.reader.avoidPageBreak ||
        previous.reader.justifyText != next.reader.justifyText ||
        previous.reader.layoutAdvanced != next.reader.layoutAdvanced ||
        previous.reader.characterSpacing != next.reader.characterSpacing ||
        previous.reader.continuousMode != next.reader.continuousMode ||
        previous.reader.hideFurigana != next.reader.hideFurigana ||
        previous.appearance.readerThemeMode != next.appearance.readerThemeMode
