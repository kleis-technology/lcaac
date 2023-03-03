package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiEmissionFactors
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiEmissionFactorsMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiEmissionFactors