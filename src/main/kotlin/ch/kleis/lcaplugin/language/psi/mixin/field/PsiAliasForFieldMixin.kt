package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiAliasForField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiAliasForFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiAliasForField
