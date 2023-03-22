package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {
    val SUBSTANCES : StubIndexKey<String, PsiSubstance> =
        StubIndexKey.createIndexKey("lca.substances")

    val TECHNO_PRODUCT_EXCHANGES : StubIndexKey<String, PsiTechnoProductExchange> =
        StubIndexKey.createIndexKey("lca.technoProductExchanges")

    val PROCESSES : StubIndexKey<String, PsiProcess> =
        StubIndexKey.createIndexKey("lca.processes")

    val UNITS : StubIndexKey<String, PsiUnitLiteral> =
        StubIndexKey.createIndexKey("lca.units")
}
