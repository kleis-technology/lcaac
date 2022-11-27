package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner
import ch.kleis.lcaplugin.psi.LcaFactors
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.tree.TokenSet

interface PsiSubstance :
    PsiUniqueIdOwner,
    PsiUnitOwner,
    StubBasedPsiElement<SubstanceStub> {
    fun getSubstanceName(): String {
        val substanceName = node.findChildByType(LcaTypes.FIELD_NAME)
        val literal = substanceName?.findChildByType(LcaTypes.STRING_LITERAL)
        return literal?.text ?: throw IllegalStateException()
    }

    fun getCompartment(): String {
        val compartment = node.findChildByType(LcaTypes.FIELD_COMPARTMENT)
        val literal = compartment?.findChildByType(LcaTypes.STRING_LITERAL)
        return literal?.text ?: throw IllegalStateException()
    }

    fun getSubcompartment(): String {
        val subcompartment = node.findChildByType(LcaTypes.FIELD_SUB_COMPARTMENT)
        val literal = subcompartment?.findChildByType(LcaTypes.STRING_LITERAL)
        return literal?.text ?: throw IllegalStateException()
    }

    fun getFactorExchanges(): Collection<PsiFactorExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.FACTORS))
            .map { it as LcaFactors }
            .flatMap { it.factorList }
            .map { it as PsiFactorExchange }
    }
}
