package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.evaluator.ToValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.simapro.SimaproImportSettings
import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.Imported
import ch.kleis.lcaplugin.imports.Importer
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.ImportedUnit
import ch.kleis.lcaplugin.imports.shared.serializer.UnitRenderer
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproSubstanceRenderer
import ch.kleis.lcaplugin.imports.util.AsyncTaskController
import ch.kleis.lcaplugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaplugin.imports.util.ImportInterruptedException
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.CountingInputStream
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import org.openlca.simapro.csv.refdata.SystemDescriptionBlock
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt


class SimaproImporter(
    private val settings: SimaproImportSettings
) : Importer() {
    private var totalValue = 1L

    companion object {
        private val LOG = Logger.getInstance(SimaproImporter::class.java)
    }

    private val processRenderer = ProcessRenderer(settings.importSubstancesMode)
    private val simaproSubstanceRenderer = SimaproSubstanceRenderer()
    private val inputParameterRenderer = InputParameterRenderer()
    private var counting: CountingInputStream? = null
    private val mapper = ToValue(BasicOperations.INSTANCE)
    private val unitRenderer = UnitRenderer.of(
        Prelude.unitMap<BasicNumber>().values
            .map { with(mapper) { it.toUnitValue() } }
            .associateBy { it.symbol.toString() }
    )

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val path = Path.of(settings.libraryFile)

        val pkg = settings.rootPackage.ifBlank { "default" }
        val fileHeaderImports = when (settings.importSubstancesMode) {
            SubstanceImportMode.EF30 -> listOf("ef30")
            SubstanceImportMode.EF31 -> listOf("ef31")
            SubstanceImportMode.SIMAPRO, SubstanceImportMode.NOTHING -> listOf()
        }
        ModelWriter(pkg, settings.rootFolder, fileHeaderImports, watcher)
            .use { w ->
                importFile(path, w, controller, watcher)
            }
    }

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectResults(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(inputParameterRenderer.nbParameters, "parameters"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(simaproSubstanceRenderer.nbSubstances, "substances")
        )
    }

    private fun importFile(
        path: Path,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
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
            SimaProCsv.read(reader) { block: CsvBlock ->
                importBlock(block, writer, controller, watcher)
            }
        }
    }


    private fun importBlock(
        block: CsvBlock,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
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
                .map {
                    ImportedUnit(it.quantity().lowercase(), it.name(), it.conversionFactor(), it.referenceUnit())
                }
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
