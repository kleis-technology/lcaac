package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class QuantityRefScopeProcessorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_whenLocalParam_thenCorrect() {
        // given
        val file = parseFile(
            "resolver", """
                package resolver
                
                process a {
                    params {
                        x = 1 kg
                    }
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first()
        val uid = process.getPsiParametersBlocks().first().getUIDs().first()
        val quantityRef = process.getProducts().first()
            .getQuantity().getTerm().getFactor().getPrimitive().getRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        TestCase.assertEquals(uid, actual)
    }

    @Test
    fun test_whenLocalVariable_thenCorrect() {
        // given
        val file = parseFile(
            "resolver", """
                package resolver
                
                process a {
                    variables {
                        x = 1 kg
                    }
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first()
        val uid = process.getPsiVariablesBlocks().first().getUIDs().first()
        val quantityRef = process.getProducts().first()
            .getQuantity().getTerm().getFactor().getPrimitive().getRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        TestCase.assertEquals(uid, actual)
    }

    @Test
    fun test_whenGlobalVariable_thenCorrect() {
        // given
        val file = parseFile(
            "resolver", """
                package resolver
                
                variables {
                    x = 1 kg
                }
                
                process a {
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val uid = file.getPsiVariablesBlocks().first().getUIDs().first()
        val process = file.getProcesses().first()
        val quantityRef = process.getProducts().first()
            .getQuantity().getTerm().getFactor().getPrimitive().getRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        TestCase.assertEquals(uid, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
