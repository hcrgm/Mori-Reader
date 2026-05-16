package app.mori.reader.ui.pages.settings.audio

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audio_delete_local_confirm
import app.mori.reader.shared.generated.resources.audio_delete_local_summary
import app.mori.reader.shared.generated.resources.audio_delete_local_title
import app.mori.reader.shared.generated.resources.audio_delete_source_confirm
import app.mori.reader.shared.generated.resources.audio_delete_source_summary
import app.mori.reader.shared.generated.resources.audio_local_not_imported
import app.mori.reader.shared.generated.resources.audio_local_summary
import app.mori.reader.shared.generated.resources.audio_mode_duck
import app.mori.reader.shared.generated.resources.audio_mode_interrupt
import app.mori.reader.shared.generated.resources.audio_mode_mix
import app.mori.reader.shared.generated.resources.cd_delete_source
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal data class AudioDeletionDialogText(
    val title: String,
    val summary: String,
    val message: String,
)

internal enum class PendingAudioDeletionType {
    Source,
    LocalDatabase,
}

internal data class PendingAudioDeletion(
    val type: PendingAudioDeletionType,
    val source: AudioSource? = null,
)

internal fun audioSourceTitle(source: AudioSource): String =
    when {
        source.isLocal -> "Local"
        else -> source.name
    }

@Composable
internal fun audioSourceSummary(source: AudioSource): String =
    when {
        source.isLocal -> stringResource(Res.string.audio_local_summary)
        else -> source.url
    }

@Composable
internal fun AudioPlaybackMode.localizedLabel(): String = stringResource(labelResource)

private val AudioPlaybackMode.labelResource: StringResource
    get() =
        when (this) {
            AudioPlaybackMode.Interrupt -> Res.string.audio_mode_interrupt
            AudioPlaybackMode.Duck -> Res.string.audio_mode_duck
            AudioPlaybackMode.Mix -> Res.string.audio_mode_mix
        }

@Composable
internal fun formatAudioDatabaseSize(bytes: Long): String =
    if (bytes <= 0L) {
        stringResource(Res.string.audio_local_not_imported)
    } else {
        formatPositiveBytes(bytes)
    }

internal fun formatPositiveBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "$bytes B"
    } else {
        "${(value * 10).toInt() / 10.0} ${units[unitIndex]}"
    }
}

@Composable
internal fun audioDeletionDialogText(deletion: PendingAudioDeletion): AudioDeletionDialogText =
    when (deletion.type) {
        PendingAudioDeletionType.Source -> {
            AudioDeletionDialogText(
                title = stringResource(Res.string.cd_delete_source),
                summary = stringResource(Res.string.audio_delete_source_summary),
                message =
                    stringResource(
                        Res.string.audio_delete_source_confirm,
                        deletion.source?.name.orEmpty(),
                    ),
            )
        }

        PendingAudioDeletionType.LocalDatabase -> {
            AudioDeletionDialogText(
                title = stringResource(Res.string.audio_delete_local_title),
                summary = stringResource(Res.string.audio_delete_local_summary),
                message = stringResource(Res.string.audio_delete_local_confirm),
            )
        }
    }
