package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.psi.stubs.StubElement

interface UnitStub : StubElement<PsiUnitDefinition> {
    val fqn : String
}
