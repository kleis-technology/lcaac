package ch.kleis.lcaplugin.language.psi.mixin.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitFactor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitFactorMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitFactor
