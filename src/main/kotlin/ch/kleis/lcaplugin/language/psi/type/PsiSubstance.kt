package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiSubstanceTypeField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.tree.TokenSet

interface PsiSubstance : BlockMetaOwner, PsiNameIdentifierOwner, StubBasedPsiElement<SubstanceStub> {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }

    fun buildUniqueKey(): String {
        return listOfNotNull(
            this.name,
            getCompartmentField().getValue(),
            getSubcompartmentField()?.getValue(),
            getTypeField().getValue(),
        ).joinToString("_")
    }

    override fun getName(): String {
        return getSubstanceRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getSubstanceRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getSubstanceRef().name = name
        return this
    }

    fun getNameField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.NAME_FIELD)?.psi as PsiStringLiteralField
    }

    fun getTypeField(): PsiSubstanceTypeField {
        return node.findChildByType(LcaTypes.TYPE_FIELD)?.psi as PsiSubstanceTypeField
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
