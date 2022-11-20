package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.ProductExchange
import com.intellij.psi.stubs.StubElement

interface ProductExchangeStub : StubElement<ProductExchange> {

    val productName : String?
}
