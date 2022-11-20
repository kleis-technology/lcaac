package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiProductExchange :
    PsiUniqueIdOwner,
    PsiUnitOwner,
    StubBasedPsiElement<ProductExchangeStub>
