package app.mori.reader.app.di

import app.mori.reader.data.anki.AndroidAnkiShareFallback
import app.mori.reader.data.anki.AnkiConnectTransport
import app.mori.reader.data.anki.AnkiDroidTransport
import app.mori.reader.data.anki.AnkiService
import app.mori.reader.data.anki.AnkiSettingsRepository
import app.mori.reader.data.anki.AnkiShareFallback
import app.mori.reader.data.anki.AnkiTransport
import app.mori.reader.data.audio.AndroidAudioRepository
import app.mori.reader.data.audio.AudioRepository
import app.mori.reader.data.audiobook.AndroidAudiobookPlayerRepository
import app.mori.reader.data.audiobook.AndroidAudiobookRepository
import app.mori.reader.data.audiobook.AudiobookPlayerRepository
import app.mori.reader.data.audiobook.AudiobookRepository
import app.mori.reader.data.book.AndroidBookRepository
import app.mori.reader.data.book.BookRepository
import app.mori.reader.data.dictionary.AndroidDictionaryRepository
import app.mori.reader.data.dictionary.DictionaryRepository
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.data.settings.createAndroidSettingsRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<SettingsRepository> { createAndroidSettingsRepository(androidApplication()) }
        single<AnkiSettingsRepository> { get<SettingsRepository>() }
        single<AudioRepository> { AndroidAudioRepository(androidApplication()) }
        single<AudiobookRepository> { AndroidAudiobookRepository(androidApplication()) }
        single<AudiobookPlayerRepository> { AndroidAudiobookPlayerRepository(androidApplication()) }
        single<BookRepository> { AndroidBookRepository(androidApplication()) }
        single<DictionaryRepository> { AndroidDictionaryRepository(androidApplication()) }
        single { AnkiDroidTransport(androidApplication()) }
        single { AnkiConnectTransport(androidApplication()) }
        single<AnkiShareFallback> { AndroidAnkiShareFallback(androidApplication()) }
        single<List<AnkiTransport>> { listOf(get<AnkiDroidTransport>(), get<AnkiConnectTransport>()) }
        single {
            AnkiService(
                settingsRepository = get(),
                transports = get(),
                shareFallback = get(),
            )
        }
    }
