package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class QuantityReferenceTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve_whenFromGlobalAssignment() {
        // given
        val pkgName = "language.psi.reference.quantity.test_resolve_whenFromGlobalAssignment"
        val fqn = "$pkgName.a"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val quantityRef = process.getProducts().first()
            .getQuantity()
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        val expected = GlobalAssigmentStubKeyIndex
            .findGlobalAssignments(project,"$pkgName.x").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenFromUnitDefinition() {
        // given
        val pkgName = "language.psi.reference.quantity.test_resolve_whenFromUnitDefinition"
        val fqn = "$pkgName.a"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val quantityRef = process.getProducts().first()
            .getQuantity()
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        val expected = UnitKeyIndex.findUnits(project, "$pkgName.x").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenFromProcessParameter() {
        // given
        val pkgName = "language.psi.reference.quantity.test_resolve_whenFromProcessParameter"
        val fqn = "$pkgName.caller"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val quantityRef = process
            .getInputs().first()
            .getFromProcessConstraint()!!
            .getAssignments().first()
            .getQuantityRef()

        // when
        val actual = quantityRef.reference?.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.called").first()
            .getPsiParametersBlocks().first()
            .getAssignments().first()
        TestCase.assertEquals(expected, actual)
    }

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/quantity"
    }
}
