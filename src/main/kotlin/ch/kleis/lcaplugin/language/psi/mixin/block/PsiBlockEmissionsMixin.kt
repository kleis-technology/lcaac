package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockEmissions
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockEmissionsMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockEmissions
