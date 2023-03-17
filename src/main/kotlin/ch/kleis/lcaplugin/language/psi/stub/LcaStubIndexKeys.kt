package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {
    val SUBSTANCES : StubIndexKey<String, PsiSubstance> =
        StubIndexKey.createIndexKey("lca.substances")
}
