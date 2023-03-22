package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType

abstract class PsiProcessMixin : StubBasedPsiElementBase<ProcessStub>, PsiProcess {
    constructor(node: ASTNode) : super(node)
    constructor(stub: ProcessStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? {
        return super<PsiProcess>.getName()
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        return super<PsiProcess>.processDeclarations(processor, state, lastParent, place)
    }
}
