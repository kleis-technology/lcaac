package com.github.albanseurat.lcaplugin.language.psi

import com.github.albanseurat.lcaplugin.language.psi.stub.ProductStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiProductElement : PsiNameIdentifierOwner, StubBasedPsiElement<ProductStub> {

    fun getUnitElement() : PsiUnitElement?
}