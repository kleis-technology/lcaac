package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiMetaBlock
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiMetaBlockMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiMetaBlock