package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import ch.kleis.lcaplugin.imports.ModelWriter
import com.intellij.openapi.diagnostic.Logger
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import java.nio.file.Path

class Importer(private val settings: LcaImportSettings) {
    companion object {
        private val LOG = Logger.getInstance(Importer::class.java)
    }


    fun importFile() {
        val path = Path.of(settings.libraryFile)
        val predefined = Prelude.unitMap.values
            .map { UnitValue(it.symbol.lowercase(), it.scale, it.dimension) }
            .associateBy { it.symbol }
        val unitRenderer = UnitRenderer.of(predefined)
        val pkg = settings.rootPackage.ifBlank { "default" }
        val writer = ModelWriter(pkg, settings.rootFolder)
        writer.use {
            SimaProCsv.read(path.toFile()) { block: CsvBlock ->
                if (block.isProcessBlock) {
                    if (settings.importProcesses) {
                        val process = block.asProcessBlock()
                        ProcessRenderer().render(process, writer)
                    }
                } else if (block.isElementaryFlowBlock) { // Resources / Substances ?
                    if (settings.importSubstances) {
                        val elementary = block.asElementaryFlowBlock()
                        SubstanceRenderer().render(elementary, writer)
                    }
                } else if (block.isQuantityBlock) { // Dimensions => no need
                    //  val elementary = block.asQuantityBlock()
                } else if (block.isCalculatedParameterBlock) { // Ecoinvent => empty
                    // val elementary = block.asCalculatedParameterBlock()
                } else if (block.isInputParameterBlock) {
                    val paramBlock = block.asInputParameterBlock()
                    InputParameterRenderer().render(paramBlock, writer)
                } else if (block.isProductStageBlock) {// Ecoinvent => empty
                    // val elementary = block.asProductStageBlock()
                } else if (block.isSystemDescriptionBlock) {// Ecoinvent => entete de library
                    val desc = block.asSystemDescriptionBlock()
                    writer.write("main", ModelWriter.pad(ModelWriter.asComment(desc.name()), 0), false)
                    writer.write("main", ModelWriter.pad(ModelWriter.asComment(desc.description()), 0), false)
//                } else if (block.isImpactMethodBlock) {
//                    val impact = block.asImpactMethodBlock()
                    // ...
                } else if (block.isUnitBlock) {
                    if (settings.importUnits) {
                        val unitBlock = block.asUnitBlock()
                        unitBlock.units()
                            .forEach { unitRenderer.render(it, writer) }
                    }
                } else {
                    LOG.warn("Missing case for ${block.javaClass}")
                }
            }
        }
    }
}