package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiSubstanceTypeField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceTypeFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceTypeField
