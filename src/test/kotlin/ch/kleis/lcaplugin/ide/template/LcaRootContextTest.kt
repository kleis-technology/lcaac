// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package ch.kleis.lcaplugin.ide.template

import ch.kleis.lcaplugin.ide.template.LcaTemplateFixture.Companion.fileWithLiveTemplateInProcess
import ch.kleis.lcaplugin.ide.template.LcaTemplateFixture.Companion.fileWithLiveTemplateInRoot
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.json.psi.JsonFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaRootContextTest : BasePlatformTestCase() {

    @Test
    fun isInContext_ShouldBeTrueInRoot_WhenCursorIsAtEndOfTheWorld() {
        // given
        val vf = myFixture.createFile("test.lca", fileWithLiveTemplateInRoot)
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val ctx = mockk<TemplateActionContext>()
        every { ctx.file } returns file
        every { ctx.startOffset } returns fileWithLiveTemplateInRoot.length - 1
        val sut = LcaRootContext()

        // When
        val result = sut.isInContext(ctx)

        // Then
        assertTrue(result)
    }

    @Test
    fun isInContext_ShouldBeFalseInProcess_WhenCursorIsAtEndOfTheWorld() {
        // given
        val vf = myFixture.createFile("test.lca", fileWithLiveTemplateInProcess)
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val ctx = mockk<TemplateActionContext>()
        every { ctx.file } returns file
        every { ctx.startOffset } returns 19 // End of 'proc'
        val sut = LcaRootContext()

        // When
        val result = sut.isInContext(ctx)

        // Then
        assertFalse(result)

    }

    @Test
    fun isInContext_ShouldBeFalseForOtherFile() {
        // given
        val vf = myFixture.createFile("test.json", "{}")
        val file = PsiManager.getInstance(project).findFile(vf) as JsonFile
        val ctx = mockk<TemplateActionContext>()
        every { ctx.file } returns file
        val sut = LcaRootContext()

        // When
        val result = sut.isInContext(ctx)

        // Then
        assertFalse(result)
    }

}