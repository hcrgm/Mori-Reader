package app.mori.reader.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Main : AppRoute

    @Serializable
    data class Reader(
        val bookId: String,
    ) : AppRoute

    @Serializable
    data object DictionarySettings : AppRoute

    @Serializable
    data object AppearanceSettings : AppRoute

    @Serializable
    data object ReaderSettings : AppRoute

    @Serializable
    data object AudioSettings : AppRoute

    @Serializable
    data object AnkiSettings : AppRoute

    @Serializable
    data object About : AppRoute

    @Serializable
    data object OpenSourceLicenses : AppRoute
}
