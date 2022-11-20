package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {

    val PRODUCT_EXCHANGES : StubIndexKey<String, PsiProductExchange> =
        StubIndexKey.createIndexKey("lca.productExchanges")

    val SUBSTANCES : StubIndexKey<String, PsiSubstance> =
        StubIndexKey.createIndexKey("lca.substances")
}
