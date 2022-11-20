package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiUniqueIdOwner : PsiNameIdentifierOwner {
    override fun getName(): String? {
        return (nameIdentifier as PsiUniqueId).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode: ASTNode? = node.findChildByType(LcaTypes.UNIQUE_ID)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${ch.kleis.lcaplugin.LcaFileType.INSTANCE.defaultExtension}",
                    ch.kleis.lcaplugin.LcaFileType.INSTANCE,
                    name
                )
            node.replaceChild(identifierNode, newIdentifier.node)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getUniqueId()
    }

    fun getUniqueId(): PsiUniqueId? {
        return node.findChildByType(LcaTypes.UNIQUE_ID)?.psi as PsiUniqueId
    }
}
