package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockMeta
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockMetaMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockMeta
