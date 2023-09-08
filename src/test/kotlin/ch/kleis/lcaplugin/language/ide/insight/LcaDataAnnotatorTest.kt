package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaDataAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testAnnotateInGlobals_whenNotFound_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_whenNotFound_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                x = q
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "Unresolved quantity reference q") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenFound_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFound_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                q = 1 kg
                x = q
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenFoundInPrelude_shouldDoNothing() {
        // given
        val pkgName = "testAnnotateInGlobals_whenFoundInPrelude_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                x = 3 kg
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex
            .findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotateInSubstance_whenNotFound_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_whenNotFound_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = unknown
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s", "Resource", "c"
        ).first()
            .getReferenceUnitField().dataExpression
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "Unresolved quantity reference unknown") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotateInSubstance_whenUnitFoundInPrelude_shouldDoNothing() {
        // given
        val pkgName = "testAnnotateInSubstance_whenUnitFoundInPrelude_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = kg
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s", "Resource", "c"
        ).first()
            .getReferenceUnitField().dataExpression
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotateInSubstance_whenFoundInExplicitDefinition_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFoundInExplicitDefinition_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = foo
            }
            
            unit foo {
                symbol = "foo"
                dimension = "foo"
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s", "Resource", "c",
        ).first()
            .getReferenceUnitField().dataExpression
        val mock = AnnotationHolderMock()
        val annotator = LcaDataAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenAlsoInPackagedUnit_shouldAnnotate() {
        // given
        val pkgName = "testAnnotateInGlobals_whenAlsoInPackagedUnit_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            unit g {
                symbol = "grm"
                alias_for = 0.001 kg
            }
        """.trimIndent()
        )
        val element = UnitStubKeyIndex.findUnits(project, "$pkgName.g")
            .first()
            .dataRef
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "This name is already defined."
            )
        }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

}
