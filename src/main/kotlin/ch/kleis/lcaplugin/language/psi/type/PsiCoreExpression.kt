package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.enums.CoreExpressionType
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiCoreExpression : PsiElement {
    fun getExpressionType(): CoreExpressionType? {
        if (node.findChildByType(LcaTypes.SYSTEM) != null) {
            return CoreExpressionType.SYSTEM
        }
        if (node.findChildByType(LcaTypes.PROCESS) != null) {
            return CoreExpressionType.PROCESS
        }
        if (node.findChildByType(LcaTypes.PRODUCT) != null) {
            return CoreExpressionType.PRODUCT
        }
        if (node.findChildByType(LcaTypes.UNIT) != null) {
            return CoreExpressionType.UNIT
        }
        if (node.findChildByType(LcaTypes.QUANTITY) != null) {
            return CoreExpressionType.QUANTITY
        }
        if (node.findChildByType(LcaTypes.VARIABLE) != null) {
            return CoreExpressionType.VARIABLE
        }
        return null
    }

    fun asSystem(): PsiSystem {
        return node.findChildByType(LcaTypes.SYSTEM)?.psi as PsiSystem
    }
    fun asProcess(): PsiProcess {
        return node.findChildByType(LcaTypes.PROCESS)?.psi as PsiProcess
    }
    fun asProduct(): PsiProduct {
        return node.findChildByType(LcaTypes.PRODUCT)?.psi as PsiProduct
    }
    fun asUnit(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit
    }
    fun asQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }
    fun asVariable(): PsiVariable {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable
    }
}
