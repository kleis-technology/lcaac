package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
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
            .fromProcessConstraint!!
            .processTemplateSpec!!

        // when
        val actual = ref.reference.resolve()

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
            .fromProcessConstraint!!
            .processTemplateSpec!!

        // when
        val actual =
            ref.reference
                .variants
                .map { (it as LookupElementBuilder).lookupString }
                .sorted()


        // then
        val expected = listOf("salad_production", "p", "carrot_production").sorted()
        TestCase.assertEquals(expected, actual.sorted())
    }

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/proc"
    }
}
