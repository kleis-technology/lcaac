package ch.kleis.lcaplugin.language.psi.mixin.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnit
