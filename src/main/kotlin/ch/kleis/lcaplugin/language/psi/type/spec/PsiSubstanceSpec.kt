package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaBlockEmissions
import ch.kleis.lcaplugin.psi.LcaBlockLandUse
import ch.kleis.lcaplugin.psi.LcaBlockResources
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.util.PsiTreeUtil

interface PsiSubstanceSpec : PsiUIDOwner {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceSpec {
        return SubstanceReferenceFromPsiSubstanceSpec(this)
    }

    fun getType(): SubstanceType? {
        return PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockEmissions }?.let { SubstanceType.EMISSION }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockResources }?.let { SubstanceType.RESOURCE }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockLandUse }?.let { SubstanceType.LAND_USE }
    }

    fun getCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getSubCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }
}
