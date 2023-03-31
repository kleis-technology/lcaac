package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import ch.kleis.lcaplugin.imports.ModelWriter
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import java.nio.file.Path

class Importer(private val settings: LcaImportSettings) {


    fun importFile() {
        val path = Path.of(settings.libraryFile)
        val predefined = Prelude.unitMap.values
            .map { UnitValue(it.symbol, it.scale, it.dimension) }
            .associateBy { it.symbol }
        val unitRenderer = UnitRenderer.of(predefined)
        val pkg = settings.rootPackage.ifBlank { "default" }
        val writer = ModelWriter(pkg, settings.rootFolder)
        writer.use {
            SimaProCsv.read(path.toFile()) { block: CsvBlock ->
                if (block.isProcessBlock) {
                    val process = block.asProcessBlock()
                    // ...
                } else if (block.isUnitBlock) {
                    if (settings.importUnits) {
                        val unitBlock = block.asUnitBlock()
                        unitBlock.units()
                            .forEach { unitRenderer.render(it, writer) }
                    }
                }
            }
        }
    }


}