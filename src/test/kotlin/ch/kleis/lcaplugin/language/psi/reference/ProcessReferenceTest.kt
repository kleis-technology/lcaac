package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Test

class ProcessReferenceTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve() {
        val pkgName = "language.psi.reference.proc.test_resolve"
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .getFromProcessConstraint()!!
            .getProcessTemplateRef()

        // when
        val actual = ref.reference?.resolve()

        // then
        val expected = ProcessStubKeyIndex
            .findProcesses(project, "$pkgName.carrot.carrot_production").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = "language.psi.reference.proc.test_resolve"
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .getFromProcessConstraint()!!
            .getProcessTemplateRef()

        // when
        val actual =
            ref.reference
                ?.variants
                ?.map { (it as LookupElementBuilder).lookupString }
                ?.sorted()
                ?: emptyList()


        // then
        val expected = listOf("salad_production", "p", "carrot_production").sorted()
        TestCase.assertEquals(expected, actual.sorted())
    }

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/proc"
    }
}
