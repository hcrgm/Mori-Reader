package app.mori.reader

import android.app.Application
import app.mori.reader.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MoriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoriApplication)
            modules(appModules())
        }
    }
}
