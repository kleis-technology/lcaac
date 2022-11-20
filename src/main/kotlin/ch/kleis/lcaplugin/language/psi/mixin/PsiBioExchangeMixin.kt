package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstanceId
import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.language.reference.SubstanceReference
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiReference

abstract class PsiBioExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBioExchange {


    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { SubstanceReference(it as PsiSubstanceId, it.textRangeInParent) }
    }

    override fun getName() : String? {
        return (nameIdentifier as PsiSubstanceId).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode : ASTNode? = getNode().findChildByType(LcaTypes.SUBSTANCE_ID)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(getProject())
                .createFileFromText("_Dummy_.${ch.kleis.lcaplugin.LcaFileType.INSTANCE.defaultExtension}", ch.kleis.lcaplugin.LcaFileType.INSTANCE, name)
            getNode().replaceChild(identifierNode, newIdentifier.node)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return node.findChildByType(LcaTypes.SUBSTANCE_ID)?.psi
    }

    override fun getUnitElement(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
            ?: throw IllegalStateException()
    }


}
