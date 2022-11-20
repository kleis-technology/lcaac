package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner
import ch.kleis.lcaplugin.language.psi.type.traits.UniqueIdOwner
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiProductExchange :
    PsiNameIdentifierOwner,
    PsiUnitOwner,
    StubBasedPsiElement<ProductExchangeStub>
