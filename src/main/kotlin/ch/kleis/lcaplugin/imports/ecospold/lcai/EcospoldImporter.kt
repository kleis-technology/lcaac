package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.core.lang.evaluator.toUnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettings
import ch.kleis.lcaplugin.imports.*
import ch.kleis.lcaplugin.imports.model.UnitImported
import ch.kleis.lcaplugin.imports.shared.UnitRenderer
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import spold2.*
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path
import javax.xml.bind.JAXB
import kotlin.math.roundToInt

class EcospoldImporter(private val settings: EcospoldImportSettings) : Importer() {
    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)

        fun unitToStr(u: String): String {
            return if (u != "metric ton*km") u else "ton*km"
        }
    }

    private var totalValue = 1
    private var currentValue = 0
    private val processRenderer = ProcessRenderer()
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
            val input1 = f.getInputStream(unitConversionFile)
            val unitConvs: UnitConversionList = JAXB.unmarshal(input1, UnitConversionList::class.java)
            fun qty(u: UnitConversion): String {
                return if (u.quantity != "lenght") u.quantity else "length"
            }
            unitConvs.conversions
                .asSequence()
                .map { UnitImported(qty(it), it.fromUnit, it.factor, unitToStr(it.toUnit)) }
                .filter { it.name != "foot-candle" }
                .forEach { unitRenderer.render(it, writer) }
        }
        entries.asSequence()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .forEach {
                importEntry(it.name, f.getInputStream(it), writer, controller, watcher)
            }
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
        val eco = EcoSpold2.read(input)
        currentValue++
        watcher.notifyProgress((100.0 * currentValue / totalValue).roundToInt())
        when {
            eco.dataSet != null -> importDataSet(eco.dataSet, w, "main", path)

            eco.childDataSet != null -> importDataSet(eco.childDataSet, w, "child", path)

            eco.impactMethod != null -> {
                importImpactMethod(eco.impactMethod, w, watcher, path)
            }
        }
    }


    private fun importImpactMethod(
        method: ImpactMethod,
        w: ModelWriter,
        watcher: AsynchronousWatcher,
        path: String
    ) {
        LOG.info("Read impactMethod ${method.name} from $path")

    }

    private fun importDataSet(
        dataSet: DataSet,
        w: ModelWriter,
        type: String,
        path: String
    ) {
        LOG.info("Read $type dataset from $path")
        processRenderer.render(dataSet, w, "$type from $path")

    }


}