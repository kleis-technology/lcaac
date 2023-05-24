package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.LcaUnitDefinition
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<LcaUnitDefinition>,
    override val fqn: String,
) : StubBase<LcaUnitDefinition>(
    parent,
    LcaTypes.UNIT_DEFINITION as IStubElementType<out StubElement<*>, *>
), UnitStub
