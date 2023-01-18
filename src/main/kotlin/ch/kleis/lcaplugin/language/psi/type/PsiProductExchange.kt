package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import com.intellij.psi.StubBasedPsiElement

interface PsiProductExchange :
    PsiTechnoExchange,
    StubBasedPsiElement<ProductExchangeStub>
