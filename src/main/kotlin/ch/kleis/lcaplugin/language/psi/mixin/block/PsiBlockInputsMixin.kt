package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockInputs
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockInputsMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockInputs
