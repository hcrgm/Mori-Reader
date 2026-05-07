package app.mori.reader.data.audiobook

import kotlinx.serialization.Serializable

@Serializable
enum class AudiobookAssetType {
    Audio,
    Subtitle,
}

@Serializable
enum class AudiobookStorageMode(
    val wireName: String,
) {
    Copy("copy"),
    Reference("reference"),
}

@Serializable
enum class AudiobookAudioFormat(
    val extension: String,
) {
    Mp3("mp3"),
    M4b("m4b"),
}

@Serializable
enum class AudiobookSubtitleFormat(
    val extension: String,
) {
    Srt("srt"),
}

@Serializable
data class AudiobookAssetInfo(
    val bookId: String,
    val type: AudiobookAssetType,
    val format: String,
    val displayName: String,
    val storageMode: AudiobookStorageMode,
    val importedAt: Long,
    val fileSizeBytes: Long,
    val mimeType: String? = null,
    val localRelativePath: String? = null,
    val sourceUriString: String? = null,
)

@Serializable
data class AudiobookCue(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val sequence: Int,
)

@Serializable
data class AudiobookSubtitleData(
    val format: AudiobookSubtitleFormat,
    val cues: List<AudiobookCue>,
    val sourceAssetDisplayName: String,
    val parsedAt: Long,
)

@Serializable
data class SasayakiMatch(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val chapterIndex: Int,
    val start: Int,
    val length: Int,
)

@Serializable
data class SasayakiCueRange(
    val id: String,
    val start: Int,
    val length: Int,
)

@Serializable
data class SasayakiMatchData(
    val matches: List<SasayakiMatch>,
    val unmatched: Int,
    val searchWindow: Int,
    val matchedAt: Long,
)

@Serializable
data class AudiobookAssetBundle(
    val audioAssetInfo: AudiobookAssetInfo? = null,
    val subtitleAssetInfo: AudiobookAssetInfo? = null,
    val subtitleData: AudiobookSubtitleData? = null,
    val matchData: SasayakiMatchData? = null,
)
