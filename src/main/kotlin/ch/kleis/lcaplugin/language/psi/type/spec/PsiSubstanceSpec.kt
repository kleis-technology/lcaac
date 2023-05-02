package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.language.psi.reference.SubstanceReference
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockEmissions
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockLandUse
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockResources
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

interface PsiSubstanceSpec : PsiElement {
    override fun getReference(): SubstanceReference {
        return SubstanceReference(this)
    }

    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }

    fun getType(): SubstanceType? {
        return PsiTreeUtil.findFirstParent(this) { p -> p is PsiBlockEmissions } ?.let { SubstanceType.EMISSION }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is PsiBlockResources } ?.let { SubstanceType.RESOURCE }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is PsiBlockLandUse } ?.let { SubstanceType.LAND_USE }
    }

    fun getCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getSubCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }
}
