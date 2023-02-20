package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiNumberField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiNumberFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiNumberField
