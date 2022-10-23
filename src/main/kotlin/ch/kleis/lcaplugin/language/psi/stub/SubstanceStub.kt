package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Substance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<Substance> {

    val substanceName : String

    val compartment: String

    val subCompartment: String?
}
