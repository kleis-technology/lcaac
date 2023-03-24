package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
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

class UnitReferenceTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_resolve() {
        // given
        val file = parseFile(
            "hello",
            """
               import my_units
               
            substance s {
                name = "s"
                compartment = "c"
                reference_unit = foo
            }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getSubstances().first()
            .getReferenceUnitField()
            .getValue().getFactor().getPrimitive().getRef()
            .reference as PsiReference
        val unitFoo = unitFoo()
        val units = listOf(unitFoo, unitBar())

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiUnitDefinition>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiUnitDefinition>>(),
            )
        } returns units

        // when
        val actual = ref.resolve()

        // then
        TestCase.assertEquals(unitFoo, actual)

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun test_getVariants() {
        // given
        val file = parseFile(
            "hello",
            """
               import my_units
               
            substance s {
                name = "s"
                compartment = "c"
                reference_unit = foo
            }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getSubstances().first()
            .getReferenceUnitField()
            .getValue().getFactor().getPrimitive().getRef()
            .reference as PsiReference

        val stubIndex = mockk<StubIndex>()
        mockkStatic(StubIndex::class)
        every {StubIndex.getInstance()} returns stubIndex
        val results = listOf("foo", "bar", "foo_2", "_1_bar")
        every {
            stubIndex.getAllKeys(
                any<StubIndexKey<String, PsiUnitDefinition>>(),
                any<Project>(),
            )
        } returns results

        // when
        val actual = ref.variants.toList().map { (it as LookupElementBuilder).lookupString }

        // then
        TestCase.assertEquals(results, actual)

        // clean
        unmockkStatic(StubIndex::class)
    }

    private fun unitFoo(): PsiUnitDefinition {
        val file = parseFile(
            "units", """
            package my_units
            
            unit foo {
                symbol = "foo"
                scale = 1.0
                dimension = "foo"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitLiterals().first()
    }

    private fun unitBar(): PsiUnitDefinition {
        val file = parseFile(
            "units", """
            package my_units_2
            
            unit foo {
                symbol = "bar"
                scale = 1.0
                dimension = "bar"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitLiterals().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
