package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.SimaProCsv

class LcaParserSimaproTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testShouldLoadAndParse() {

        val datasetUrl = this.javaClass.classLoader.getResource("test.csv")

        val reader = SimaProCsv.readerOf(datasetUrl.openStream(), SimaProCsv.defaultCharset())

        val header = CsvHeader.readFrom(reader)

        val datasets = SimaProCsv.read(header, reader)

        datasets.processes().forEach {

            val datasetContent = """
                dataset "${it.name()}" {
                    
                    meta {
                        category: "${it.category()}"
                        allocationRules: "${it.allocationRules()}"
                        collectionMethod: "${it.collectionMethod()}"
                        comment: "${it.comment().replace("\"", "\\\"")}"
                        
                    }
                }
            """.trimIndent()

            parseFile(
                "${it.name()}.${LcaFileType.INSTANCE.defaultExtension}",
                datasetContent
            )
            assertEquals(toParseTreeText(myFile, skipSpaces(), includeRanges()),
                0, myFile.node.getChildren(TokenSet.create(TokenType.BAD_CHARACTER)).size)
        }

    }


    override fun getTestDataPath(): String {
        return ""
    }
}