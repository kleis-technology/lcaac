package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiSubstance: PsiElement {
    fun getUid(): PsiUID {
        return node.findChildByType(LcaTypes.UID)!!.psi as PsiUID
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
