package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.psi.LcaUnitDefinition
import com.intellij.psi.stubs.StubElement

interface UnitStub : StubElement<LcaUnitDefinition> {
    val fqn: String
}
