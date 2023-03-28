package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProcessStubImpl(
    parent: StubElement<PsiProcess>,
    override val fqn: String,
) :
        StubBase<PsiProcess>(parent, LcaTypes.PROCESS as IStubElementType<out StubElement<*>, *>),
        ProcessStub
