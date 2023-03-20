package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.psi.stubs.StubElement

interface TechnoProductExchangeStub : StubElement<PsiTechnoProductExchange> {
    val uid : String
}
