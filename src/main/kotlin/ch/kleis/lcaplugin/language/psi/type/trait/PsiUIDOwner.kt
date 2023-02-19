package ch.kleis.lcaplugin.language.psi.type.trait

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiUIDOwner : PsiNameIdentifierOwner {
    fun getUID(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID
    }

    override fun getName(): String? {
        return (nameIdentifier as PsiUID).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode: ASTNode? = node.findChildByType(LcaTypes.UID)
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
        return getUID()
    }
}
