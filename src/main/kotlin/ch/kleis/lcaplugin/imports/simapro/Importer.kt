package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import ch.kleis.lcaplugin.ide.imports.SubstanceImportMode
import ch.kleis.lcaplugin.imports.ImportInterruptedException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.simapro.substance.AsyncTaskController
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproSubstanceRenderer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.CountingInputStream
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.refdata.SystemDescriptionBlock
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt

data class Imported(val qty: Int, val name: String)
sealed class Summary(
    val durationInSec: Long,
    private val importedResources: List<Imported> = listOf()
) {
    fun getResourcesAsString(): String {
        return importedResources.filter { it.qty > 0 }
            .joinToString(", ") { "${it.qty} ${it.name}" }
    }

}

class SummaryInSuccess(
    durationInSec: Long,
    importedResources: List<Imported>,
) : Summary(durationInSec, importedResources)

class SummaryInterrupted(
    durationInSec: Long,
    importedResources: List<Imported>,
) : Summary(durationInSec, importedResources)

class SummaryInError(
    durationInSec: Long,
    importedResources: List<Imported>,
    val errorMessage: String
) : Summary(durationInSec, importedResources)

class Importer(
    private val settings: LcaImportSettings,
    private val watcher: AsynchronousWatcher,
    private val controller: AsyncTaskController
) {
    private val begin = Instant.now()
    private var totalValue = 1L

    companion object {
        private val LOG = Logger.getInstance(Importer::class.java)
    }

    private val processRenderer = ProcessRenderer(settings.importSubstancesMode)
    private val simaproSubstanceRenderer = SimaproSubstanceRenderer()
    private val inputParameterRenderer = InputParameterRenderer()
    private var counting: CountingInputStream? = null
    private val unitRenderer = UnitRenderer.of(
        Prelude.unitMap.values
            .map { UnitValue(ModelWriter.sanitize(it.symbol, false), it.scale, it.dimension) }
            .associateBy { it.symbol.lowercase() }
    )

    fun import(): Summary {
        try {
            val path = Path.of(settings.libraryFile)

            val pkg = settings.rootPackage.ifBlank { "default" }
            val fileHeaderImports = when (settings.importSubstancesMode) {
                SubstanceImportMode.EF30 -> listOf("ef30")
                SubstanceImportMode.EF31 -> listOf("ef31")
                SubstanceImportMode.SIMAPRO, SubstanceImportMode.NOTHING -> listOf()
            }
            ModelWriter(pkg, settings.rootFolder, fileHeaderImports, watcher)
                .use { w ->
                    importFile(path, w, unitRenderer)
                }
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            return SummaryInSuccess(duration, collectProgress())
        } catch (e: ImportInterruptedException) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            return SummaryInterrupted(duration, collectProgress())
        } catch (e: Exception) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            return SummaryInError(duration, collectProgress(), e.message ?: "")
        }
    }

    private fun collectProgress(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(inputParameterRenderer.nbParameters, "parameters"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(simaproSubstanceRenderer.nbSubstances, "substances")
        )


    }

    private fun importFile(path: Path, writer: ModelWriter, unitRenderer: UnitRenderer) {
        val file = path.toFile()
        totalValue = file.length()
        val input = file.inputStream()
        input.use {
            val countingVal = CountingInputStream(input)
            val realInput: InputStream = when {
                file.path.endsWith(".gz") -> GZIPInputStream(countingVal)
                file.path.endsWith(".zip") -> {
                    val zip = ZipInputStream(countingVal); zip.nextEntry; zip
                }

                else -> countingVal
            }
            counting = countingVal
            val reader = InputStreamReader(realInput)
            SimaproStreamCsvReader.read(reader, { block: CsvBlock ->
                importBlock(block, writer, unitRenderer)
            })
        }
    }


    private fun importBlock(block: CsvBlock, writer: ModelWriter, unitRenderer: UnitRenderer) {
        val read = counting?.bytesRead ?: 0L
        watcher.notifyProgress((100.0 * read / totalValue).roundToInt())

        if (!controller.isActive()) throw ImportInterruptedException()
        when {
            block.isProcessBlock && settings.importProcesses -> processRenderer.render(
                block.asProcessBlock(),
                writer
            )

            block.isElementaryFlowBlock ->
                if (settings.importSubstancesMode == SubstanceImportMode.SIMAPRO)
                    simaproSubstanceRenderer.render(block.asElementaryFlowBlock(), writer)

            block.isUnitBlock && settings.importUnits -> block.asUnitBlock().units()
                .forEach { unitRenderer.render(it, writer) }

            block.isInputParameterBlock -> inputParameterRenderer.render(block.asInputParameterBlock(), writer)
            block.isSystemDescriptionBlock -> renderMain(block.asSystemDescriptionBlock(), writer)
            block.isQuantityBlock -> { /* Dimensions => no need */
            }

            block.isCalculatedParameterBlock -> {/* Ecoinvent => empty*/
            }

            block.isProductStageBlock -> { /* Ecoinvent => empty */
            }

            block.isImpactMethodBlock -> { /* Ecoinvent => empty */
            }

            else -> LOG.warn("Missing case for ${block.javaClass}")
        }
    }

    private fun renderMain(block: SystemDescriptionBlock?, writer: ModelWriter) {
        block?.let {
            writer.write("main", ModelWriter.pad(ModelWriter.asComment(block.name()), 0), false)
            writer.write("main", ModelWriter.pad(ModelWriter.asComment(block.description()), 0), false)
        }
    }


}