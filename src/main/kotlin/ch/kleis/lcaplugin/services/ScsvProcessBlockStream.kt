package ch.kleis.lcaplugin.services

import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.CsvLine
import org.openlca.simapro.csv.SimaProCsv
import org.openlca.simapro.csv.process.ProcessBlock
import java.io.InputStream
import java.util.function.Consumer

class ScsvProcessBlockStream(
    private val consumer: Consumer<ProcessBlock>
) {
    fun read(inputStream: InputStream) {
        val reader = SimaProCsv.readerOf(inputStream, SimaProCsv.defaultCharset())
        reader.use {
            val header = CsvHeader.readFrom(reader)
            val iter = CsvLine.iter(header, reader)
            for (line in iter) {
                if (line.first().equals("Process")) {
                    val block = ProcessBlock.read(iter)
                    this.consumer.accept(block)
                    continue
                }
            }
        }
    }
}
