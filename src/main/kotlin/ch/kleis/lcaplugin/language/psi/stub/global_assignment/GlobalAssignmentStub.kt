package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import com.intellij.psi.stubs.StubElement

interface GlobalAssignmentStub : StubElement<PsiGlobalAssignment> {
    val fqn : String
}
