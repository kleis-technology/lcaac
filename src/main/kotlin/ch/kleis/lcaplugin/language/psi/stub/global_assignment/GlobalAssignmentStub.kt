package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.psi.LcaGlobalAssignment
import com.intellij.psi.stubs.StubElement

interface GlobalAssignmentStub : StubElement<LcaGlobalAssignment> {
    val fqn: String
}
