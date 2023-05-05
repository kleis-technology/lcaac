package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiAllocateField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiAllocateFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiAllocateField
