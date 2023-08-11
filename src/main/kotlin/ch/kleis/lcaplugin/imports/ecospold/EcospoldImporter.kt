package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.core.lang.evaluator.toUnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.EcospoldImportSettings
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.LCIASettings
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.LCISettings
import ch.kleis.lcaplugin.imports.Imported
import ch.kleis.lcaplugin.imports.Importer
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lci.EcospoldMethodMapper
import ch.kleis.lcaplugin.imports.ecospold.lci.MappingExchange
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.model.Parser
import ch.kleis.lcaplugin.imports.model.ImportedUnit
import ch.kleis.lcaplugin.imports.shared.serializer.UnitRenderer
import ch.kleis.lcaplugin.imports.util.AsyncTaskController
import ch.kleis.lcaplugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaplugin.imports.util.ImportInterruptedException
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.io.input.BOMInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class EcospoldImporter(
    private val settings: EcospoldImportSettings,
) : Importer() {

    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)

        fun unitToStr(u: String): String {
            return if (u != "metric ton*km") u else "ton*km"
        }

        fun getMethodNames(libFile: String): List<String> {
            val path = Path.of(libFile)
            try {
                SevenZFile(path.toFile()).use { f ->
                    val methodsFile = f.entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
                    f.getInputStream(methodsFile).use {
                        return Parser.readMethodName(it)
                    }
                }
            } catch (e: Exception) {
                return listOf("")
            }
        }
    }

    private var totalValue = 1
    private var currentValue = 0
    private val processRenderer = EcospoldProcessRenderer()
    private val methodName: String = when (settings) {
        is LCISettings -> "Ecospold LCI library file."
        is LCIASettings -> settings.methodName
    }
    private val predefinedUnits = Prelude.unitMap.values
        .map { it.toUnitValue() }
        .associateBy { it.symbol.toString() }
    private val unitRenderer = UnitRenderer.of(predefinedUnits)

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val methodMapping =
            if (settings is LCISettings && settings.mappingFile.isNotEmpty()) {
                buildMapping(watcher, settings)
            } else {
                null
            }

        val path = Path.of(settings.libraryFile)
        val pkg = settings.rootPackage.ifBlank { "default" }
        SevenZFile(path.toFile()).use { f ->
            ModelWriter(pkg, settings.rootFolder, builtinLibraryImports(settings), watcher).use { w ->
                importEntries(f, methodMapping, w, controller, watcher)
            }
        }
    }

    private fun buildMapping(watcher: AsynchronousWatcher, settings: LCISettings): Map<String, MappingExchange> {
        watcher.notifyCurrentWork("Building requested method map")
        FileInputStream(settings.mappingFile).use {
            val bomIS = BOMInputStream(it)
            val isr = InputStreamReader(bomIS, StandardCharsets.UTF_8)

            return EcospoldMethodMapper.buildMapping(isr)
        }
    }

    private fun builtinLibraryImports(settings: EcospoldImportSettings): List<String> =
        if (settings is LCISettings && settings.importBuiltinLibrary != null) {
            listOf(settings.importBuiltinLibrary.toString())
        } else listOf()

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectResults(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(processRenderer.nbProcesses, "substances"),
        )
    }

    private fun importEntries(
        f: SevenZFile,
        methodMapping: Map<String, MappingExchange>?,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val start = Instant.now()
        val entries = f.entries.toList()
        totalValue = entries.size

        processRenderer.processDict = readProcessDict(f, entries)

        if (settings.importUnits) {
            importUnits(entries, f, writer)
        }

        val parsedEntries = entries.asSequence()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .map {
                (it.name to importEntry(f.getInputStream(it), controller, watcher))
            }

        val methodMappedEntries = methodMapping?.let {
            getMethodMappedEntries(it, parsedEntries)
        }

        (methodMappedEntries ?: parsedEntries).forEach { it: Pair<String, ActivityDataset> ->
            writeImportedDataset(it.second, writer, it.first)
        }

        val duration = Duration.between(start, Instant.now())
        renderMain(writer, unitRenderer.nbUnit, processRenderer.nbProcesses, methodName, duration)
    }

    private fun getMethodMappedEntries(
        methodMapping: Map<String, MappingExchange>,
        parsedEntries: Sequence<Pair<String, ActivityDataset>>
    ): Sequence<Pair<String, ActivityDataset>> =
        parsedEntries.map { (fileName, activityDataset) ->
            fileName to activityDataset.copy(
                flowData = activityDataset.flowData.copy(
                    elementaryExchanges = activityDataset.flowData.elementaryExchanges.map { originalExchange ->
                        methodMapping[originalExchange.elementaryExchangeId]?.let { mapping ->
                            originalExchange.copy(
                                amount = mapping.conversionFactor?.let { it * originalExchange.amount }
                                    ?: originalExchange.amount,
                                name = mapping.name ?: originalExchange.name,
                                unit = mapping.unit ?: originalExchange.unit,
                                compartment = mapping.compartment ?: originalExchange.compartment,
                                subCompartment = mapping.subCompartment
                                    ?: originalExchange.subCompartment?.ifEmpty { null },
                                comment = originalExchange.comment?.let { it + "\n" + mapping.comment }
                                    ?: mapping.comment
                            )
                        } ?: originalExchange
                    }
                )
            )
        }

    private fun importUnits(entries: List<SevenZArchiveEntry>, f: SevenZFile, writer: ModelWriter) {
        val unitConversionFile = entries.firstOrNull { it.name.endsWith("UnitConversions.xml") }
        val fromMeta = f.getInputStream(unitConversionFile).use {
            val unitConvs = Parser.readUnits(it)
            unitConvs.asSequence()
                .map { u ->
                    ImportedUnit(
                        u.dimension, u.fromUnit, u.factor,
                        unitToStr(u.toUnit)
                    )
                }
                .filter { u -> u.name != "foot-candle" }
        }

        val methodsFile = entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
        val fromMethod = f.getInputStream(methodsFile).use {
            val unitConvs = Parser.readMethodUnits(it, methodName)
            unitConvs.asSequence()
                .map { u ->
                    ImportedUnit(
                        u.dimension, u.fromUnit, u.factor,
                        unitToStr(u.toUnit)
                    )
                }
                .filter { u -> u.name != "foot-candle" }
                .filter { u -> !predefinedUnits.containsKey(u.name) }
        }

        (fromMeta + fromMethod)
            .distinctBy { it.name }
            .forEach { unitRenderer.render(it, writer) }
    }

    private fun renderMain(writer: ModelWriter, nbUnits: Int, nbProcess: Int, methodName: String, duration: Duration) {
        val s = duration.seconds
        val durAsStr = String.format("%02dm %02ds", s / 60, (s % 60))
        val block = """
            Import Method: $methodName
            Date: ${ZonedDateTime.now()}
            Import Summary:
                * $nbUnits units
                * $nbProcess processes
                * $nbProcess substances
            Duration: $durAsStr
        """.trimIndent()

        writer.write("main", ModelWriter.pad(ModelWriter.asComment(block), 0), false)
    }

    data class ProcessDictRecord(
        val processId: String,
        val fileName: String,
        val processName: String,
        val geo: String,
        val productName: String
    )

    private fun readProcessDict(f: SevenZFile, entries: List<SevenZArchiveEntry>): Map<String, ProcessDictRecord> {
        val dictEntry = entries.first { it.name.endsWith("FilenameToActivityLookup.csv") }
        val csvFormat = CSVFormat.Builder.create().setDelimiter(";").setHeader().build()
        val records = CSVParser.parse(f.getInputStream(dictEntry), Charset.defaultCharset(), csvFormat)
        return records.map {
            ProcessDictRecord(
                it["Filename"].substring(0, it["Filename"].indexOf("_")),
                it["Filename"],
                it["ActivityName"],
                it["Location"],
                it["ReferenceProduct"],
            )
        }.associateBy { it.processId }
    }

    private fun importEntry(
        input: InputStream,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ): ActivityDataset {
        if (!controller.isActive()) throw ImportInterruptedException()
        val activityDataset: ActivityDataset = Parser.readDataset(input)

        currentValue++
        watcher.notifyProgress((100.0 * currentValue / totalValue).roundToInt())
        return activityDataset
    }

    private fun writeImportedDataset(
        dataSet: ActivityDataset,
        w: ModelWriter,
        path: String
    ) {
        LOG.info("Read dataset from $path")
        processRenderer.render(dataSet, w, "from $path", methodName)
    }
}