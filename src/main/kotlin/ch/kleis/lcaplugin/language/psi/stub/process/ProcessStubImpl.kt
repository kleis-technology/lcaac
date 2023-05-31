package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProcessStubImpl(
    parent: StubElement<LcaProcess>,
    override val key: ProcessKey,
) :
    StubBase<LcaProcess>(parent, LcaTypes.PROCESS as IStubElementType<out StubElement<*>, *>),
    ProcessStub
