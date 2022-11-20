package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.ProductExchange
import ch.kleis.lcaplugin.language.psi.type.Substance
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {

    val PRODUCT_EXCHANGES : StubIndexKey<String, ProductExchange> =
        StubIndexKey.createIndexKey("lca.productExchanges")

    val SUBSTANCES : StubIndexKey<String, Substance> =
        StubIndexKey.createIndexKey("lca.substances")
}
