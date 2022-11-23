package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiSubstanceIdOwner : PsiNameIdentifierOwner {
    override fun getName(): String? {
        return (nameIdentifier as PsiUniqueId).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode: ASTNode? = node.findChildByType(LcaTypes.UNIQUE_ID)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${LcaFileType.INSTANCE.defaultExtension}",
                    LcaFileType.INSTANCE,
                    name
                )
            node.replaceChild(identifierNode, newIdentifier.node)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return node.findChildByType(LcaTypes.UNIQUE_ID)?.psi
    }
}
