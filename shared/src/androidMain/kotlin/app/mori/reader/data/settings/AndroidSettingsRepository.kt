package app.mori.reader.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createAndroidSettingsRepository(context: Context): SettingsRepository =
    SettingsRepository(AndroidSettingsDataStore.get(context.applicationContext))

private object AndroidSettingsDataStore {
    private var dataStore: DataStore<Preferences>? = null

    fun get(context: Context): DataStore<Preferences> =
        dataStore ?: createSettingsDataStore(
            producePath = {
                context.filesDir.resolve(SETTINGS_DATA_STORE_FILE_NAME).absolutePath
            },
        ).also { dataStore = it }
}
