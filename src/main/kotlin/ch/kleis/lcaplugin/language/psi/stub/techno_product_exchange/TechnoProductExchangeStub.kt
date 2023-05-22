package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.psi.LcaTechnoProductExchange
import com.intellij.psi.stubs.StubElement

interface TechnoProductExchangeStub : StubElement<LcaTechnoProductExchange> {
    val fqn: String
}
