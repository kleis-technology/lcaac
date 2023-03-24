package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<PsiUnitDefinition>,
    override val uid: String,
) : StubBase<PsiUnitDefinition>(
    parent,
    LcaTypes.UNIT_DEFINITION as IStubElementType<out StubElement<*>, *>
), UnitStub
