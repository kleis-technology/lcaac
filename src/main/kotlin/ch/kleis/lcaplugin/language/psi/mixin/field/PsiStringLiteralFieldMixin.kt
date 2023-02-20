package ch.kleis.lcaplugin.language.psi.mixin.field

import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiStringLiteralFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiStringLiteralField
