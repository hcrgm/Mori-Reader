package app.mori.reader.ui.navigation

import androidx.navigation3.runtime.NavKey
import app.mori.reader.ui.AppTab
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Main : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Dictionary : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object HomeDetail : AppRoute

    @Serializable
    data class Reader(val bookId: String) : AppRoute

    @Serializable
    data object DictionarySettings : AppRoute

    @Serializable
    data object AppearanceSettings : AppRoute

    @Serializable
    data object AudioSettings : AppRoute

    @Serializable
    data object AnkiSettings : AppRoute
}

fun AppTab.toRoute(): AppRoute = when (this) {
    AppTab.Home -> AppRoute.Home
    AppTab.Dictionary -> AppRoute.Dictionary
    AppTab.Settings -> AppRoute.Settings
}

fun AppRoute.toTabOrNull(): AppTab? = when (this) {
    AppRoute.Home -> AppTab.Home
    AppRoute.Dictionary -> AppTab.Dictionary
    AppRoute.Settings -> AppTab.Settings
    AppRoute.Main,
    AppRoute.HomeDetail,
    is AppRoute.Reader,
    AppRoute.DictionarySettings,
    AppRoute.AppearanceSettings,
    AppRoute.AudioSettings,
    AppRoute.AnkiSettings,
    -> null
}
