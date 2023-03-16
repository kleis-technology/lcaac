package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class QuantityReference(
    element: PsiQuantityRef
) : PsiReferenceBase<PsiQuantityRef>(element) {
    override fun resolve(): PsiElement? {
        val resolver = QuantityRefScopeProcessor(element)
        var lastParent : PsiElement? = null
        val parents = PsiTreeUtil.collectParents(element, PsiElement::class.java, false) {
            it.elementType == LcaFile::elementType
        }
        for (parent in parents) {
            val doBreak = !parent.processDeclarations(resolver, ResolveState.initial(), lastParent, element)
            if (doBreak) {
                break
            }
            lastParent = parent
        }
        return resolver.getResult()
    }
}
