package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<PsiUnitLiteral>,
    override val uid: String,
) : StubBase<PsiUnitLiteral>(
    parent,
    LcaTypes.UNIT_LITERAL as IStubElementType<out StubElement<*>, *>
), UnitStub
