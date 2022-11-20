package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class SubstanceStubImpl(
    parent: StubElement<PsiSubstance>,
    override val substanceName: String,
    override val compartment: String?,
    override val subCompartment: String?
) :
    StubBase<PsiSubstance>(parent, LcaTypes.SUBSTANCE as IStubElementType<*, *>),
    SubstanceStub
