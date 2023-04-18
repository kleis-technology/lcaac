package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import ch.kleis.lcaplugin.imports.ModelWriter
import com.intellij.openapi.diagnostic.Logger
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import org.openlca.simapro.csv.refdata.SystemDescriptionBlock
import java.nio.file.Path

class Importer(private val settings: LcaImportSettings) {
    companion object {
        private val LOG = Logger.getInstance(Importer::class.java)
    }


    fun import() {
        val path = Path.of(settings.libraryFile)
        val predefined = Prelude.unitMap.values
            .map { UnitValue(ModelWriter.sanitize(it.symbol, false), it.scale, it.dimension) }
            .associateBy { it.symbol.lowercase() }
        val unitRenderer = UnitRenderer.of(predefined)
        val pkg = settings.rootPackage.ifBlank { "default" }
        val writer = ModelWriter(pkg, settings.rootFolder)
        writer.use {
            importFile(path, it, unitRenderer)
        }
    }

    private fun importFile(path: Path, writer: ModelWriter, unitRenderer: UnitRenderer) {
        SimaProCsv.read(path.toFile()) { block: CsvBlock ->
            importBlock(block, writer, unitRenderer)
        }
    }

    @SuppressWarnings("kotlin:S108", "Ignore empty block bellow")
    private fun importBlock(block: CsvBlock, writer: ModelWriter, unitRenderer: UnitRenderer) {
        when {
            block.isProcessBlock && settings.importProcesses -> ProcessRenderer().render(block.asProcessBlock(), writer)
            block.isElementaryFlowBlock && settings.importSubstances ->
                SubstanceRenderer().render(block.asElementaryFlowBlock(), writer)

            block.isUnitBlock && settings.importUnits -> block.asUnitBlock().units()
                .forEach { unitRenderer.render(it, writer) }

            block.isInputParameterBlock -> InputParameterRenderer().render(block.asInputParameterBlock(), writer)
            block.isSystemDescriptionBlock -> renderMain(block.asSystemDescriptionBlock(), writer)
            block.isQuantityBlock -> {} // Dimensions => no need
            block.isCalculatedParameterBlock -> {} // Ecoinvent => empty
            block.isProductStageBlock -> {} // Ecoinvent => empty
            block.isImpactMethodBlock -> {} // Ecoinvent => empty

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