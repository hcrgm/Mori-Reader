package app.mori.reader.app.di

import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiViewModel
import app.mori.reader.features.audiobook.presentation.AudiobookViewModel
import app.mori.reader.features.bookshelf.presentation.BookshelfViewModel
import app.mori.reader.features.dictionary.domain.DictionaryLookupUseCase
import app.mori.reader.features.dictionary.presentation.DictionaryViewModel
import app.mori.reader.features.reader.presentation.ReaderViewModel
import app.mori.reader.features.settings.presentation.SettingsViewModel
import app.mori.reader.ui.RootViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val bookshelfModule =
    module {
        viewModel {
            BookshelfViewModel(
                bookRepository = get(),
                settingsRepository = get(),
            )
        }
    }

private val dictionaryModule =
    module {
        factory { DictionaryLookupUseCase(dictionaryRepository = get()) }
        viewModel {
            DictionaryViewModel(
                dictionaryRepository = get(),
                lookupText = get(),
                settingsRepository = get(),
            )
        }
    }

private val readerModule =
    module {
        viewModel { params ->
            ReaderViewModel(
                bookId = params.get(),
                bookRepository = get(),
                lookupText = get(),
                audiobookRepository = get(),
                audiobookPlayerRepository = get(),
                settingsRepository = get(),
            )
        }
    }

private val audiobookModule =
    module {
        viewModel { params ->
            AudiobookViewModel(
                audiobookRepository = get(),
                settingsRepository = get(),
                preferredStorageMode =
                    params.getOrNull<AudiobookStorageMode>()
                        ?: AudiobookStorageMode.Copy,
            )
        }
    }

private val ankiModule =
    module {
        viewModel {
            AnkiViewModel(
                settingsRepository = get(),
                ankiService = get(),
            )
        }
    }

private val settingsModule =
    module {
        viewModel {
            SettingsViewModel(
                settingsRepository = get(),
                dictionaryRepository = get(),
                audioRepository = get(),
            )
        }
    }

private val rootModule =
    module {
        viewModel { params ->
            RootViewModel(
                settingsRepository = get(),
                initialSettings = params.getOrNull<AppSettings>(),
            )
        }
    }

fun appModules(): List<Module> =
    listOf(
        platformModule(),
        bookshelfModule,
        dictionaryModule,
        readerModule,
        audiobookModule,
        ankiModule,
        settingsModule,
        rootModule,
    )
