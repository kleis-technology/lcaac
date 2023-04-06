package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test


class ParameterReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/parameter"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = "language.psi.reference.parameter.test_resolve"
        val fqn = "$pkgName.p"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val ref = process.getInputs().first()
            .getFromProcessConstraint()!!
            .getPsiArguments().first()
            .getParameterRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.water_prod").first()
            .getPsiParametersBlocks().first()
            .getAssignments().first()
        TestCase.assertEquals(expected, actual)
    }
}
