package app.mori.reader.data.dictionary

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import de.manhhao.hoshi.HoshiDicts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.UUID
import java.util.zip.ZipException
import java.util.zip.ZipFile

internal class AndroidDictionaryRepository(
    private val context: Context,
) : DictionaryRepository {
    private val dictionariesRoot: File = File(context.filesDir, "Dictionaries")
    private val configFile: File = File(dictionariesRoot, "config.json")
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }

    override suspend fun loadDictionaries(): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            dictionariesRoot.mkdirs()
            val config = loadConfig()
            val catalog =
                DictionaryCatalog(
                    termDictionaries =
                        collectDictionaries(
                            DictionaryType.Term,
                            config.termDictionaries,
                        ),
                    frequencyDictionaries =
                        collectDictionaries(
                            DictionaryType.Frequency,
                            config.frequencyDictionaries,
                        ),
                    pitchDictionaries =
                        collectDictionaries(
                            DictionaryType.Pitch,
                            config.pitchDictionaries,
                        ),
                )
            rebuildQuery(catalog)
            catalog
        }

    override suspend fun importDictionaries(
        type: DictionaryType,
        uriStrings: List<String>,
        onProgress: ((DictionaryImportProgress) -> Unit)?,
    ): DictionaryImportResult =
        withContext(Dispatchers.IO) {
            val outputDir = typeDirectory(type).also { it.mkdirs() }
            val failures = mutableListOf<DictionaryImportFailure>()
            var successCount = 0

            uriStrings.forEachIndexed { index, uriString ->
                val uri = Uri.parse(uriString)
                val displayName = displayName(uri) ?: uri.decodedFallbackName("词典-${index + 1}.zip")
                val tempZip =
                    File(context.cacheDir, "dictionary-import-${System.nanoTime()}-$index.zip")
                onProgress?.invoke(
                    DictionaryImportProgress(
                        currentIndex = index + 1,
                        totalCount = uriStrings.size,
                    ),
                )
                try {
                    copyUriToFile(uri, tempZip)
                    val result =
                        HoshiDicts.importDictionary(tempZip.absolutePath, outputDir.absolutePath)
                    if (!result.success) {
                        failures +=
                            DictionaryImportFailure(
                                fileName = displayName,
                                reason = classifyImportFailure(tempZip),
                            )
                    } else {
                        successCount += 1
                    }
                } catch (throwable: Throwable) {
                    failures +=
                        DictionaryImportFailure(
                            fileName = displayName,
                            reason = classifyImportFailure(tempZip, throwable),
                        )
                } finally {
                    tempZip.delete()
                }
            }

            val catalog = scanAndPersist()
            rebuildQuery(catalog)
            DictionaryImportResult(
                catalog = catalog,
                successCount = successCount,
                failures = failures,
            )
        }

    override suspend fun setEnabled(
        type: DictionaryType,
        id: String,
        enabled: Boolean,
    ): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            val catalog =
                mutate(type) { dictionaries ->
                    dictionaries.map { dictionary ->
                        if (dictionary.id == id) dictionary.copy(isEnabled = enabled) else dictionary
                    }
                }
            rebuildQuery(catalog)
            catalog
        }

    override suspend fun move(
        type: DictionaryType,
        id: String,
        direction: MoveDirection,
    ): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            val catalog =
                mutate(type) { dictionaries ->
                    val currentIndex = dictionaries.indexOfFirst { it.id == id }
                    if (currentIndex == -1) return@mutate dictionaries
                    val targetIndex =
                        when (direction) {
                            MoveDirection.Up -> currentIndex - 1
                            MoveDirection.Down -> currentIndex + 1
                        }
                    if (targetIndex !in dictionaries.indices) return@mutate dictionaries

                    dictionaries
                        .toMutableList()
                        .also {
                            val moved = it.removeAt(currentIndex)
                            it.add(targetIndex, moved)
                        }.withOrder()
                }
            rebuildQuery(catalog)
            catalog
        }

    override suspend fun reorder(
        type: DictionaryType,
        ids: List<String>,
    ): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            val catalog =
                mutate(type) { dictionaries ->
                    val orderById = ids.withIndex().associate { (index, id) -> id to index }
                    dictionaries.sortedWith(
                        compareBy<DictionaryInfo> { orderById[it.id] ?: Int.MAX_VALUE }
                            .thenBy { it.order },
                    )
                }
            rebuildQuery(catalog)
            catalog
        }

    override suspend fun delete(
        type: DictionaryType,
        id: String,
    ): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            val catalog = loadCatalogOnly()
            val dictionary =
                catalog.dictionaries(type).firstOrNull { it.id == id }
                    ?: return@withContext catalog
            File(dictionary.path).deleteRecursively()
            val refreshed = scanAndPersist()
            rebuildQuery(refreshed)
            refreshed
        }

    override suspend fun updateDictionaries(): DictionaryCatalog =
        withContext(Dispatchers.IO) {
            val before = loadCatalogOnly()
            val updatable =
                DictionaryType.entries.flatMap { type ->
                    before.dictionaries(type).filter { it.isUpdatable }.map { type to it }
                }
            updatable.forEach { (type, dictionary) ->
                runCatching {
                    val remoteIndex =
                        URL(dictionary.index.indexUrl).readText().let {
                            json.decodeFromString(DictionaryIndex.serializer(), it)
                        }
                    if (remoteIndex.revision == dictionary.index.revision || remoteIndex.downloadUrl.isBlank()) return@runCatching

                    val tempZip =
                        File(context.cacheDir, "dictionary-update-${UUID.randomUUID()}.zip")
                    try {
                        URL(remoteIndex.downloadUrl).openStream().use { input ->
                            tempZip.outputStream().use { output -> input.copyTo(output) }
                        }
                        HoshiDicts.importDictionary(
                            tempZip.absolutePath,
                            typeDirectory(type).absolutePath,
                        )
                    } finally {
                        tempZip.delete()
                    }
                }
            }
            val catalog = scanAndPersist()
            rebuildQuery(catalog)
            catalog
        }

    override suspend fun rebuildQuery(catalog: DictionaryCatalog) {
        withContext(Dispatchers.IO) {
            HoshiDicts.rebuildQuery(
                HoshiDicts.lookupObject,
                catalog.termDictionaries.enabledPaths(),
                catalog.frequencyDictionaries.enabledPaths(),
                catalog.pitchDictionaries.enabledPaths(),
            )
        }
    }

    override suspend fun lookup(
        text: String,
        maxResults: Int,
    ): DictionaryLookupResult =
        withContext(Dispatchers.IO) {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) {
                return@withContext DictionaryLookupResult()
            }
            val entries =
                HoshiDicts
                    .lookup(HoshiDicts.lookupObject, trimmed, maxResults, scanLength = 16)
                    .map { result ->
                        val term = result.term
                        DictionaryLookupEntry(
                            expression = term.expression,
                            reading = term.reading,
                            matched = result.matched,
                            deinflectionTrace =
                                result.process.reversed().map {
                                    DictionaryTraceStep(name = it.name)
                                },
                            glossaries =
                                term.glossaries.map {
                                    DictionaryGlossary(
                                        dictionary = it.dictName,
                                        content = it.glossary,
                                        definitionTags = it.definitionTags,
                                        termTags = it.termTags,
                                    )
                                },
                            frequencies =
                                term.frequencies.map {
                                    DictionaryFrequencyGroup(
                                        dictionary = it.dictName,
                                        frequencies =
                                            it.frequencies.map { frequency ->
                                                DictionaryFrequency(
                                                    value = frequency.value,
                                                    displayValue = frequency.displayValue,
                                                )
                                            },
                                    )
                                },
                            pitches =
                                term.pitches.map {
                                    DictionaryPitchGroup(
                                        dictionary = it.dictName,
                                        pitchPositions = it.pitchPositions.toList().distinct(),
                                    )
                                },
                            rules = term.rules.split(' ').filter { it.isNotBlank() },
                        )
                    }
            val styles =
                HoshiDicts
                    .getStyles(HoshiDicts.lookupObject)
                    .associate { it.dictName to it.styles }
            DictionaryLookupResult(entries = entries, styles = styles)
        }

    private fun scanAndPersist(): DictionaryCatalog {
        val config = loadConfig()
        val catalog =
            DictionaryCatalog(
                termDictionaries =
                    collectDictionaries(
                        DictionaryType.Term,
                        config.termDictionaries,
                    ),
                frequencyDictionaries =
                    collectDictionaries(
                        DictionaryType.Frequency,
                        config.frequencyDictionaries,
                    ),
                pitchDictionaries =
                    collectDictionaries(
                        DictionaryType.Pitch,
                        config.pitchDictionaries,
                    ),
            )
        saveConfig(catalog)
        return catalog
    }

    private fun mutate(
        type: DictionaryType,
        block: (List<DictionaryInfo>) -> List<DictionaryInfo>,
    ): DictionaryCatalog {
        val catalog = loadCatalogOnly()
        val updated =
            when (type) {
                DictionaryType.Term -> catalog.copy(termDictionaries = block(catalog.termDictionaries).withOrder())
                DictionaryType.Frequency -> catalog.copy(frequencyDictionaries = block(catalog.frequencyDictionaries).withOrder())
                DictionaryType.Pitch -> catalog.copy(pitchDictionaries = block(catalog.pitchDictionaries).withOrder())
            }
        saveConfig(updated)
        return updated
    }

    private fun loadCatalogOnly(): DictionaryCatalog {
        val config = loadConfig()
        return DictionaryCatalog(
            termDictionaries = collectDictionaries(DictionaryType.Term, config.termDictionaries),
            frequencyDictionaries =
                collectDictionaries(
                    DictionaryType.Frequency,
                    config.frequencyDictionaries,
                ),
            pitchDictionaries = collectDictionaries(DictionaryType.Pitch, config.pitchDictionaries),
        )
    }

    private fun collectDictionaries(
        type: DictionaryType,
        configEntries: List<DictionaryConfigEntry>,
    ): List<DictionaryInfo> {
        val stored = readStoredDictionaries(type)
        val byFileName = stored.associateBy { it.fileName }
        val result = mutableListOf<DictionaryInfo>()

        configEntries.sortedBy { it.order }.forEach { entry ->
            val dictionary = byFileName[entry.fileName] ?: return@forEach
            result +=
                dictionary.copy(
                    isEnabled = entry.isEnabled,
                    order = result.size,
                )
        }

        val configured = result.mapTo(mutableSetOf()) { it.fileName }
        stored.forEach { dictionary ->
            if (dictionary.fileName !in configured) {
                result += dictionary.copy(isEnabled = true, order = result.size)
            }
        }

        return result.withOrder()
    }

    private fun readStoredDictionaries(type: DictionaryType): List<DictionaryInfo> {
        val directory = typeDirectory(type).also { it.mkdirs() }
        return directory
            .listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dictionaryDirectory ->
                val marker = File(dictionaryDirectory, ".hoshidicts_1")
                val indexFile = File(dictionaryDirectory, "index.json")
                if (!marker.exists() || !indexFile.isFile) {
                    dictionaryDirectory.deleteRecursively()
                    return@mapNotNull null
                }
                val index =
                    runCatching {
                        json.decodeFromString(DictionaryIndex.serializer(), indexFile.readText())
                    }.getOrNull()
                if (index == null || index.title.isBlank()) {
                    dictionaryDirectory.deleteRecursively()
                    return@mapNotNull null
                }
                DictionaryInfo(
                    id = dictionaryDirectory.name,
                    index = index,
                    path = dictionaryDirectory.absolutePath,
                    fileName = dictionaryDirectory.name,
                )
            }?.sortedBy { it.index.title.lowercase() }
            .orEmpty()
    }

    private fun loadConfig(): DictionaryConfig =
        runCatching {
            if (configFile.isFile) {
                json.decodeFromString(DictionaryConfig.serializer(), configFile.readText())
            } else {
                DictionaryConfig()
            }
        }.getOrDefault(DictionaryConfig())

    private fun saveConfig(catalog: DictionaryCatalog) {
        dictionariesRoot.mkdirs()
        val config =
            DictionaryConfig(
                termDictionaries = catalog.termDictionaries.toConfigEntries(),
                frequencyDictionaries = catalog.frequencyDictionaries.toConfigEntries(),
                pitchDictionaries = catalog.pitchDictionaries.toConfigEntries(),
            )
        configFile.writeText(json.encodeToString(DictionaryConfig.serializer(), config))
    }

    private fun typeDirectory(type: DictionaryType): File = File(dictionariesRoot, type.directoryName)

    private fun displayName(uri: Uri): String? =
        runCatching {
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index)?.takeIf(String::isNotBlank) else null
                }
        }.getOrNull()

    private fun classifyImportFailure(
        tempZip: File,
        throwable: Throwable? = null,
    ): DictionaryImportFailureReason {
        if (throwable is SecurityException || throwable is IOException) {
            return DictionaryImportFailureReason.UnreadableFile
        }

        return runCatching {
            ZipFile(tempZip).use { zip ->
                val hasIndex =
                    zip.entries().asSequence().any { entry ->
                        !entry.isDirectory && entry.name.substringAfterLast('/') == "index.json"
                    }
                when {
                    !hasIndex -> DictionaryImportFailureReason.UnsupportedFile
                    throwable is ZipException -> DictionaryImportFailureReason.CorruptedFile
                    throwable != null -> DictionaryImportFailureReason.CorruptedFile
                    else -> DictionaryImportFailureReason.Unknown
                }
            }
        }.getOrElse {
            when (throwable) {
                is ZipException -> DictionaryImportFailureReason.CorruptedFile
                is SecurityException, is IOException -> DictionaryImportFailureReason.UnreadableFile
                else -> DictionaryImportFailureReason.CorruptedFile
            }
        }
    }

    private fun copyUriToFile(
        uri: Uri,
        target: File,
    ) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取词典文件" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
    }
}

private fun Uri.decodedFallbackName(defaultName: String): String =
    Uri
        .decode(lastPathSegment.orEmpty())
        .substringAfterLast('/')
        .substringAfterLast(':')
        .substringBefore('?')
        .ifBlank { defaultName }

private fun List<DictionaryInfo>.withOrder(): List<DictionaryInfo> = mapIndexed { index, dictionary -> dictionary.copy(order = index) }

private fun List<DictionaryInfo>.toConfigEntries(): List<DictionaryConfigEntry> =
    mapIndexed { index, dictionary ->
        DictionaryConfigEntry(
            fileName = dictionary.fileName,
            isEnabled = dictionary.isEnabled,
            order = index,
        )
    }

private fun List<DictionaryInfo>.enabledPaths(): Array<String> =
    filter { it.isEnabled }
        .sortedBy { it.order }
        .map { it.path }
        .toTypedArray()
