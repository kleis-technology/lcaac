package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface Product : PsiNameIdentifierOwner, StubBasedPsiElement<ProductStub> {

    fun getUnitElement() : PsiUnitElement?
}
