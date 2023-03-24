package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ModelWriter
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import java.nio.file.Path

class Importer {

    fun importFile(pkg: String, root: String, file: String) {

        val path = Path.of(file)
        val predefined = Prelude.unitMap.values
            .map { UnitValue(it.symbol, it.scale, it.dimension) }
            .associateBy { it.symbol }
        val unitRenderer = UnitRenderer.of(predefined)
        val writer = ModelWriter(pkg, root)
        writer.use {
            SimaProCsv.read(path.toFile()) { block: CsvBlock ->
                if (block.isProcessBlock) {
                    val process = block.asProcessBlock()
                    // ...
                } else if (block.isUnitBlock) {
                    val unitBlock = block.asUnitBlock()
                    unitBlock.units()
                        .forEach { unitRenderer.render(it, writer) }
                }
            }
        }
    }


}