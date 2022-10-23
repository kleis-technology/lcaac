package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductStub
import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface Substance : PsiNameIdentifierOwner, StubBasedPsiElement<SubstanceStub> {

    fun getUnitElement() : PsiUnitElement?
}
