package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiQuantityField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityField
