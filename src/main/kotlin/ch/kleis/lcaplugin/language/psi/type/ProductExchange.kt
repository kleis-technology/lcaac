package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface ProductExchange : PsiNameIdentifierOwner, StubBasedPsiElement<ProductExchangeStub> {

    fun getUnitElement() : PsiUnitElement?
}
