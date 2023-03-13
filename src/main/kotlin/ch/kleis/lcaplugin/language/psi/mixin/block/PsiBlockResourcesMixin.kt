package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockResources
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockResourcesMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockResources
