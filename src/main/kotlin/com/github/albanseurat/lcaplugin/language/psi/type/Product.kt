package com.github.albanseurat.lcaplugin.language.psi.type

import com.github.albanseurat.lcaplugin.language.psi.stub.ProductStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface Product : PsiNameIdentifierOwner, StubBasedPsiElement<ProductStub> {

    fun getUnitElement() : PsiUnitElement?
}