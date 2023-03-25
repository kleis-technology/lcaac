package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiGlobalAssignmentMixin : StubBasedPsiElementBase<GlobalAssignmentStub>, PsiGlobalAssignment {
    constructor(node: ASTNode) : super(node)
    constructor(stub: GlobalAssignmentStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiGlobalAssignment>.getName()
    }
}
