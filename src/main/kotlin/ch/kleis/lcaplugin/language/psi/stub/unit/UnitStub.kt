package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.psi.stubs.StubElement

interface UnitStub : StubElement<PsiUnitLiteral> {
    val uid : String
}
