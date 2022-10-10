package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.SimaProCsv

fun String.sanitize() : String {
    return this.replace("\"", "\\\"").trim('\r','\n')
}

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
                        - category: "${it.category().name.sanitize()}"
                        - allocationRules: "${it.allocationRules().sanitize()}"
                        - collectionMethod: "${it.collectionMethod().sanitize()}"
                        - comment: "${it.comment().sanitize()}"
                        - dataTreatment: "${it.dataTreatment().sanitize()}"
                        - date: "${it.date()}"
                        - generator: "${it.generator().sanitize()}"
                        - identifier: "${it.identifier().sanitize()}"
                        - infrastructure: "${it.infrastructure()}"
                        - platformId: "${it.platformId()?.sanitize()}"
                        - processType: ${it.processType()}
                        - record: "${it.record().sanitize()}"
                        - status: "${it.status()}"
                        - systemDescription: "${it.systemDescription()}"
                        - verification: "${it.verification().sanitize()}"
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