package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.psi.LcaQuantityRef
import ch.kleis.lcaplugin.psi.LcaScaleQuantityExpression
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QuantityReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/quantity"
    }

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
        val ref = process.getProducts().first()
            .getQuantity() as LcaQuantityRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = GlobalAssigmentStubKeyIndex
            .findGlobalAssignments(project, "$pkgName.x").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenFromUnitDefinition() {
        // given
        val pkgName = "language.psi.reference.quantity.test_resolve_whenFromUnitDefinition"
        val fqn = "$pkgName.a"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val assignment = process
            .getProducts().first()
            .getQuantity() as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef

        // when
        val actual = ref.reference.resolve()

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
        val ref = process
            .getInputs().first()
            .fromProcessConstraint!!
            .argumentList.first()
            .parameterRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.called").first()
            .getLcaParams().first()
            .assignmentList.first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenInSubstanceReferenceUnitField() {
        // given
        val pkgName = "language.psi.reference.units.test_resolve"
        val ref = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s",
            "Resource",
            "c"
        ).first()
            .getReferenceUnitField()
            .quantityExpression

        // when
        val actual = ref.reference!!.resolve()

        // then
        val expected = UnitKeyIndex.findUnits(project, "$pkgName.my_units.foo").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants_whenInSustanceReferenceField() {
        // given
        val pkgName = "language.psi.reference.units.test_resolve"
        val ref = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s",
            "Resource",
            "c"
        ).first()
            .getReferenceUnitField()
            .quantityExpression

        // when
        val actual = ref.reference!!.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf("foo", "bar").sorted()
        TestCase.assertEquals(expected, actual)
    }
}
