package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.psi.LcaGlobalAssignment
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class GlobalAssignmentStubImpl(
    parent: StubElement<LcaGlobalAssignment>,
    override val fqn: String,
) : StubBase<LcaGlobalAssignment>(parent, LcaTypes.GLOBAL_ASSIGNMENT as IStubElementType<out StubElement<*>, *>),
    GlobalAssignmentStub
