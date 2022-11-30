package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.type.PsiUrn
import ch.kleis.lcaplugin.compute.urn.Namespace
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiUrnOwner : PsiNameIdentifierOwner {
    fun getUrnElement(): PsiUrn {
        return node.findChildByType(LcaTypes.URN)?.psi as PsiUrn?
            ?: throw IllegalStateException()
    }

    override fun getNameIdentifier(): PsiElement? {
        return getUrnElement()
    }

    override fun getName(): String? {
        return getUrnElement().getParts().joinToString(Namespace.SEPARATOR)
    }

    override fun setName(name: String): PsiElement {
        val urnElement: ASTNode? = node.findChildByType(LcaTypes.URN)
        if (urnElement != null) {
            val newUrnElement = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${LcaFileType.INSTANCE.defaultExtension}",
                    LcaFileType.INSTANCE,
                    name
                )
            node.replaceChild(urnElement, newUrnElement.node)
        }
        return this
    }
}
