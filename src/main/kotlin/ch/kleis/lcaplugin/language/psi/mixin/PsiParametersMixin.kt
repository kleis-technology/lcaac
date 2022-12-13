package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiParametersMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiParameters {
}
