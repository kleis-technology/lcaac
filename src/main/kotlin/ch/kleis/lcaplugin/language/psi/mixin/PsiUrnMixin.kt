package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.language.psi.type.PsiUrn
import ch.kleis.lcaplugin.lib.registry.Namespace
import ch.kleis.lcaplugin.lib.registry.URN
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUrnMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUrn {
    override fun getUrn(rootNs: Namespace): URN {
        return rootNs.append(getLocalUrn())
    }

    override fun getLocalUrn(): URN {
        val first = node.findChildByType(LcaTypes.UNIQUE_ID) as PsiUniqueId
        val localRootName = first.name ?: throw IllegalStateException()
        val localRootNs = Namespace(localRootName, null)
        val next = node.findChildByType(LcaTypes.URN) as PsiUrn? ?: return localRootNs.selfUrn()
        return  localRootNs.append(next.getLocalUrn())
    }
}
