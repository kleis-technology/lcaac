package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory

abstract class PsiProcessMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcess {
    override fun getName() : String? {
        return (nameIdentifier as PsiUniqueId).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode : ASTNode? = getNode().findChildByType(ch.kleis.lcaplugin.psi.LcaTypes.UNIQUE_ID)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(getProject())
                .createFileFromText("_Dummy_.${ch.kleis.lcaplugin.LcaFileType.INSTANCE.defaultExtension}", ch.kleis.lcaplugin.LcaFileType.INSTANCE, name)
            getNode().replaceChild(identifierNode, newIdentifier.node)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return node.findChildByType(ch.kleis.lcaplugin.psi.LcaTypes.UNIQUE_ID)?.psi
    }

}
