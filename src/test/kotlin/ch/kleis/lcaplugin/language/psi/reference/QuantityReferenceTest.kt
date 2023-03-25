package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Test

class QuantityReferenceTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_resolve_whenFromGlobalAssignment() {
        // given
        val file = parseFile(
            "resolver", """
                package resolver
                
                import other
                
                process a {
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val globalVariableX = globalVariableX()
        val process = file.getProcesses().first()
        val quantityRef = process.getProducts().first()
            .getQuantity().getTerm().getFactor().getPrimitive().getRef()

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                eq(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                eq(PsiGlobalAssignment::class.java),
            )
        } answers {
            val target= it.invocation.args[1]
            if (target == "other.x") {
                listOf(globalVariableX)
            } else emptyList()
        }
        every {
            StubIndex.getElements(
                eq(LcaStubIndexKeys.UNITS),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                eq(PsiUnitDefinition::class.java),
            )
        } returns emptyList()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        TestCase.assertEquals(globalVariableX, actual)

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun test_resolve_whenFromUnitDefinition() {
        // given
        val file = parseFile(
            "resolver", """
                package resolver
                
                import other
                
                process a {
                    products {
                        3 x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val unitX = unitX()
        val process = file.getProcesses().first()
        val quantityRef = process.getProducts().first()
            .getQuantity().getTerm().getFactor().getPrimitive().getRef()

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                eq(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                eq(PsiGlobalAssignment::class.java),
            )
        } returns emptyList()
        every {
            StubIndex.getElements(
                eq(LcaStubIndexKeys.UNITS),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                eq(PsiUnitDefinition::class.java),
            )
        } answers {
            val target= it.invocation.args[1]
            if (target == "other.x") {
                listOf(unitX)
            } else emptyList()
        }

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        TestCase.assertEquals(unitX, actual)

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun globalVariableX(): PsiGlobalAssignment {
        val file = parseFile(
            "resolver", """
                package other
                
                variables {
                    x = 10 kg
                }
            """.trimIndent()
        ) as LcaFile
        return file.getPsiGlobalVariablesBlocks().first().getGlobalAssignments().first()
    }

    private fun unitX(): PsiUnitDefinition {
        val file = parseFile(
            "resolver", """
                package other
                
                unit x {
                    symbol = "x"
                    dimension = "x"
                }
            """.trimIndent()
        ) as LcaFile
        return file.getUnitDefinitions().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
