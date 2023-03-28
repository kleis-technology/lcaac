package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiGlobalVariables
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiGlobalVariablesMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiGlobalVariables
