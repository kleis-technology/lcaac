package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.psi.LcaSubstance
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class SubstanceStubImpl(
    parent: StubElement<LcaSubstance>,
    override val key: SubstanceKey
) :
    StubBase<LcaSubstance>(parent, LcaTypes.SUBSTANCE as IStubElementType<*, *>),
    SubstanceStub
