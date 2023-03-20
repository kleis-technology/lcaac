package ch.kleis.lcaplugin.language.ide.insight

import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import io.mockk.every
import io.mockk.mockk

class AnnotationHolderMock {
    val holder = mockk<AnnotationHolder>()
    val builder = mockk<AnnotationBuilder>()

    init {
        every { holder.newAnnotation(any(), any()) } returns builder

        every { holder.newAnnotation(any(), any()) } returns builder
        every { builder.range(any<PsiElement>()) } returns builder
        every { builder.highlightType(any()) } returns builder
        every { builder.create() } returns Unit
    }
}
