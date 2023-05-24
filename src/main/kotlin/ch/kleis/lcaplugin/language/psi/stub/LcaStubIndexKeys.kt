package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKey
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {
    val SUBSTANCES: StubIndexKey<SubstanceKey, LcaSubstance> =
        StubIndexKey.createIndexKey("lca.substances")

    val TECHNO_PRODUCT_EXCHANGES: StubIndexKey<String, LcaTechnoProductExchange> =
        StubIndexKey.createIndexKey("lca.technoProductExchanges")

    val PROCESSES: StubIndexKey<String, LcaProcess> =
        StubIndexKey.createIndexKey("lca.processes")

    val UNITS: StubIndexKey<String, LcaUnitDefinition> =
        StubIndexKey.createIndexKey("lca.units")

    val GLOBAL_ASSIGNMENTS: StubIndexKey<String, LcaGlobalAssignment> =
        StubIndexKey.createIndexKey("lca.globalAssignments")
}
