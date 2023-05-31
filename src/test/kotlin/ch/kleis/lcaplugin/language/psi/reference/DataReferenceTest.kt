package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.psi.LcaDataRef
import ch.kleis.lcaplugin.psi.LcaScaleQuantityExpression
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataReferenceTest : BasePlatformTestCase() {

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
            .dataExpression as LcaDataRef

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
            .dataExpression as LcaScaleQuantityExpression
        val ref = assignment.dataExpression!! as LcaDataRef

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
            .inputProductSpec
            .fromProcessConstraint!!
            .processTemplateSpec!!
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
            .dataExpression

        // when
        val actual = ref.reference!!.resolve()

        // then
        val expected = UnitKeyIndex.findUnits(project, "$pkgName.my_units.foo").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants_whenInSubstanceReferenceField() {
        // given
        val pkgName = "language.psi.reference.units.test_resolve"
        val ref = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s",
            "Resource",
            "c"
        ).first()
            .getReferenceUnitField()
            .dataExpression

        // when
        val actual = ref.reference!!.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = setOf("foo", "bar")
        TestCase.assertEquals(expected, actual.toSet())
    }
}
