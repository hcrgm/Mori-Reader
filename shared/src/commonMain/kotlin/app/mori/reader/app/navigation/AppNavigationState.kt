package app.mori.reader.app.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.navigation.AppRoute

@Stable
class AppNavigationState {
    val rootBackStack: SnapshotStateList<NavKey> = mutableStateListOf(AppRoute.Main)

    private var dictionaryVerticalScrollActive by mutableStateOf(false)

    fun pushRoot(route: NavKey) {
        rootBackStack.add(route)
    }

    fun openReader(bookId: String) {
        pushRoot(AppRoute.Reader(bookId))
    }

    fun popRoot() {
        if (rootBackStack.size > 1) {
            rootBackStack.removeLastOrNull()
        }
    }

    fun onDictionaryScrollActiveChange(active: Boolean) {
        dictionaryVerticalScrollActive = active
    }

    fun onCurrentTabChanged(tab: AppTab) {
        if (tab != AppTab.Dictionary) {
            dictionaryVerticalScrollActive = false
        }
    }

    fun canSwipeTabs(hasDictionaryPopup: Boolean): Boolean = !dictionaryVerticalScrollActive && !hasDictionaryPopup
}
