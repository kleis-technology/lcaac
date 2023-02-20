package ch.kleis.lcaplugin.language.psi.mixin.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitPrimitive
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitPrimitiveMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitPrimitive
