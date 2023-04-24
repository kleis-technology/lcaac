package ch.kleis.lcaplugin.imports.simapro

import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.CsvLine
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.method.ImpactMethodBlock
import org.openlca.simapro.csv.process.ProcessBlock
import org.openlca.simapro.csv.process.ProductStageBlock
import org.openlca.simapro.csv.refdata.*
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.function.Consumer

private const val BUFFER_SIZE_FOR_HEADER = 1024 * 1024

/* This class should be merged in openlca repo.
This is a hack awaiting this PR to be merged: https://github.com/GreenDelta/olca-simapro-csv/pull/22/files
 */
class SimaproStreamCsvReader {
    companion object {

        @Throws(IOException::class)
        fun read(rawReader: Reader, fn: Consumer<CsvBlock>, bufferSize: Int = BUFFER_SIZE_FOR_HEADER) {
            val reader = BufferedReader(rawReader)
            reader.mark(bufferSize)
            val header = CsvHeader.readFrom(reader)
            reader.reset()
            val iter = CsvLine.iter(header, reader)
            for (line in iter) {
                if (line.first() == "Project Calculated parameters") {
                    val block = CalculatedParameterBlock.readProjectParameters(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Database Calculated parameters") {
                    val block = CalculatedParameterBlock.readDatabaseParameters(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Method") {
                    val block = ImpactMethodBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Project Input parameters") {
                    val block = InputParameterBlock.readProjectParameters(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Database Input parameters") {
                    val block = InputParameterBlock.readDatabaseParameters(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Process") {
                    val block = ProcessBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Product stage") {
                    val block = ProductStageBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Quantities") {
                    val block = QuantityBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "System description") {
                    val block = SystemDescriptionBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                if (line.first() == "Units") {
                    val block = UnitBlock.read(iter)
                    fn.accept(block)
                    continue
                }
                val type = ElementaryFlowType.of(line.first())
                if (type != null) {
                    val block = ElementaryFlowBlock.read(type, iter)
                    fn.accept(block)
                }
            }
        }
    }
}