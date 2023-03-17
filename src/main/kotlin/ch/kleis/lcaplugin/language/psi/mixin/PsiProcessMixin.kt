package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

abstract class PsiProcessMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcess {
    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        return super<PsiProcess>.processDeclarations(processor, state, lastParent, place)
    }

    override fun getName(): String {
        return super<PsiProcess>.getName()
    }
}
