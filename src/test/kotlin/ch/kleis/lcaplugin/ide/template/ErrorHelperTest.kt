package ch.kleis.lcaplugin.ide.template

import ch.kleis.lcaplugin.ide.template.LcaTemplateFixture.Companion.fileWithLiveTemplateInProcess
import ch.kleis.lcaplugin.ide.template.LcaTemplateFixture.Companion.fileWithLiveTemplateInRoot
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ErrorHelperTest : BasePlatformTestCase() {


    @Test
    fun isInErrorInRootBlock_ShouldBeTrueInRoot_WhenErrorInRoot() {
        // given
        val vf = myFixture.createFile("test.lca", fileWithLiveTemplateInRoot)
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val elt = file.findElementAt(fileWithLiveTemplateInRoot.length - 1)

        // When
        val result = ErrorHelper.isInErrorInRootBlock(elt)

        // Then
        Assert.assertTrue(result)
    }
    //LcaTokenType.process, LcaTokenType.substance, LcaTokenType.unit or LcaTokenType.variables expected, got 'proc'
//        PsiErrorElement:LcaTokenType.emissions, LcaTokenType.inputs, LcaTokenType.labels, LcaTokenType.land_use, LcaTokenType.meta, LcaTokenType.parameters, LcaTokenType.products, LcaTokenType.resources, LcaTokenType.right-bracket or LcaTokenType.variables expected, got 'proc'
//        return true

    @Test
    fun isInContext_ShouldBeFalseInProcess_WhenErrorInProcess() {
        // given
        val vf = myFixture.createFile("test.lca", fileWithLiveTemplateInProcess)
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val elt = file.findElementAt(19)

        // When
        val result = ErrorHelper.isInErrorInRootBlock(elt)


        // Then
        Assert.assertFalse(result)

    }
}