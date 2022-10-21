package com.github.albanseurat.lcaplugin.language.psi.type

import com.github.albanseurat.lcaplugin.language.psi.stub.ProductStub
import com.github.albanseurat.lcaplugin.language.psi.stub.SubstanceStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface Substance : PsiNameIdentifierOwner, StubBasedPsiElement<SubstanceStub> {

    fun getUnitElement() : PsiUnitElement?
}