package ch.kleis.lcaplugin.language.psi.mixin.block

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockEmissions
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockLandUse
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBlockLandUseMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiBlockLandUse
