package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.tree.TokenSet

interface PsiSubstance: PsiUIDOwner, StubBasedPsiElement<SubstanceStub> {
    fun getNameField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.NAME_FIELD)?.psi as PsiStringLiteralField
    }

    fun getCompartmentField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField
    }

    fun getSubcompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getReferenceUnitField(): PsiUnitField {
        return node.findChildByType(LcaTypes.REFERENCE_UNIT_FIELD)?.psi as PsiUnitField
    }

    fun hasImpacts(): Boolean {
        return node.findChildByType(LcaTypes.BLOCK_IMPACTS) != null
    }
    fun getBlockImpacts(): Collection<PsiBlockImpacts> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_IMPACTS))
            .map { it.psi as PsiBlockImpacts }
    }

    fun getImpactExchanges(): Collection<PsiImpactExchange> {
        return getBlockImpacts()
            .flatMap { it.getExchanges() }
    }
}
