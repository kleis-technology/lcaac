package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import com.intellij.psi.stubs.StubElement

interface ProductExchangeStub : StubElement<PsiProductExchange> {
    val productName : String?
}
