package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.core.lang.evaluator.toUnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettings
import ch.kleis.lcaplugin.imports.*
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.UnitImported
import ch.kleis.lcaplugin.imports.shared.UnitRenderer
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.math.roundToInt

private const val METHOD_NAME = "EF v3.1"

class EcospoldImporter(private val settings: EcospoldImportSettings) : Importer() {
    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)

        fun unitToStr(u: String): String {
            return if (u != "metric ton*km") u else "ton*km"
        }
    }

    private var totalValue = 1
    private var currentValue = 0
    private val processRenderer = Ecospold2ProcessRenderer()
    private val unitRenderer = UnitRenderer.of(
        Prelude.unitMap.values
            .map { it.toUnitValue() }
            .associateBy { it.symbol.toString() }
    )

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val path = Path.of(settings.libraryFile)

        val pkg = settings.rootPackage.ifBlank { "default" }
        SevenZFile(path.toFile()).use { f ->
            ModelWriter(pkg, settings.rootFolder, listOf(), watcher).use { w ->
                importEntries(f, w, controller, watcher)
            }
        }
    }

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectProgress(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(processRenderer.nbProcesses, "processes"),
        )
    }

    private fun importEntries(
        f: SevenZFile,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val entries = f.entries.toList()
        totalValue = entries.size

        processRenderer.processDict = readProcessDict(f, entries)

        if (settings.importUnits) {
            val unitConversionFile = entries.firstOrNull { it.name.endsWith("UnitConversions.xml") }
            val fromMeta = f.getInputStream(unitConversionFile).use {
                val unitConvs = Parser.readUnits(it)
                unitConvs.asSequence()
                    .map { u -> UnitImported(u.dimension, u.fromUnit, u.factor, unitToStr(u.toUnit)) }
                    .filter { u -> u.name != "foot-candle" }
            }
            val methodsFile = entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
            val fromMethod = f.getInputStream(methodsFile).use {
                val unitConvs = Parser.readMethodUnits(it, METHOD_NAME)
                unitConvs.asSequence()
                    .map { u -> UnitImported(u.dimension, u.fromUnit, u.factor, unitToStr(u.toUnit)) }
                    .filter { u -> u.name != "foot-candle" }
            }

            (fromMeta + fromMethod)
                .distinctBy { it.name }
                .forEach { unitRenderer.render(it, writer) }
        }
        entries.asSequence()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .forEach {
                importEntry(it.name, f.getInputStream(it), writer, controller, watcher)
            }
        // TODO next PR: Add main with import info
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
        path: String,
        input: InputStream,
        w: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        if (!controller.isActive()) throw ImportInterruptedException()
        val eco = Parser.readDataset(input)
        currentValue++
        watcher.notifyProgress((100.0 * currentValue / totalValue).roundToInt())
        importDataSet(eco.activityDataset, w, path)
    }

    private fun importDataSet(
        dataSet: ActivityDataset,
        w: ModelWriter,
        path: String
    ) {
        LOG.info("Read dataset from $path")
        processRenderer.render(dataSet, w, "from $path", METHOD_NAME)

    }


}