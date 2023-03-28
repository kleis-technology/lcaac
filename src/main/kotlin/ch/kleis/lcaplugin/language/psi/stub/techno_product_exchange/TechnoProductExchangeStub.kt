package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.psi.stubs.StubElement

interface TechnoProductExchangeStub : StubElement<PsiTechnoProductExchange> {
    val fqn : String
}
