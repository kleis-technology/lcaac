package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.psi.LcaSubstance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<LcaSubstance> {
    val key: SubstanceKey
}
