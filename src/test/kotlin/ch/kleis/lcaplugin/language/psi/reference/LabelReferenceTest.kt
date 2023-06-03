package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LabelReferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_whenInsideBlockLabels() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    labels {
                        geo = "FR"
                    }
                }    
            """.trimIndent()
        )
        val labelAssignment = ProcessStubKeyIndex.findProcesses(
            project, "$pkgName.p",
            mapOf("geo" to "FR"),
        ).first()
            .labelsList.first()
            .labelAssignmentList.first()
        val ref = labelAssignment
            .getLabelRef()

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(labelAssignment, actual)
    }

    @Test
    fun test_resolve_whenInsideMathLabels() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production match (geo = "FR")
                    }
                }    
                
                process carrot_production {
                    labels {
                        geo = "FR"
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(
            project, "$pkgName.p",
        ).first().getInputs()
            .first()
            .inputProductSpec.getProcessTemplateSpec()!!
            .getMatchLabels()!!.labelSelectorList.first()
            .labelRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(
            project, "$pkgName.carrot_production", mapOf("geo" to "FR"),
        ).first()
            .labelsList.first()
            .labelAssignmentList.first()
        assertEquals(expected, actual)
    }
}
