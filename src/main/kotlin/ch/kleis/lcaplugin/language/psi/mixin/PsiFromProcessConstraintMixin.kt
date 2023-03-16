package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiFromProcessConstraintMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiFromProcessConstraint
