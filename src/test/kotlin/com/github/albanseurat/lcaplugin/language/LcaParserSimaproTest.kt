package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.SimaProCsv

class LcaParserSimaproTest : ParsingTestCase("", "lca", LcaParserDefinition())  {

    @Test
    fun testShouldLoadAndParse() {

        val datasetUrl = this.javaClass.classLoader.getResource("test.csv")

        val reader = SimaProCsv.readerOf(datasetUrl.openStream(), SimaProCsv.defaultCharset())

        val header = CsvHeader.readFrom(reader)

        val datasets = SimaProCsv.read(header, reader)

        val firstProcess = datasets.processes()[0]


        parseFile("${firstProcess.name()}.${LcaFileType.INSTANCE.defaultExtension}",
            """
                dataset ${firstProcess.name()} {
                
                }
            """.trimIndent())

        assertEquals("", toParseTreeText(myFile, skipSpaces(), includeRanges()))

    }


    override fun getTestDataPath(): String {
        return ""
    }
}