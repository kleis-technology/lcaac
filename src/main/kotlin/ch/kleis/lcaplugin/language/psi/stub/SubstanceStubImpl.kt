package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Substance
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class SubstanceStubImpl(parent: StubElement<Substance>, override val substanceName: String,
                        override val compartment: String, override val subCompartment: String?) :
    StubBase<Substance>(parent, ch.kleis.lcaplugin.psi.LcaTypes.SUBSTANCE as IStubElementType<*, *>), SubstanceStub {

}
