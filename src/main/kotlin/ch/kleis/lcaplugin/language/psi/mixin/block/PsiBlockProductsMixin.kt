package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockProducts
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockProductsMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockProducts
