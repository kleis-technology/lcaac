package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiUID
import ch.kleis.lcaplugin.language.psi.type.PsiVariables
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

class QuantityRefScopeProcessor(
    private val quantityRef: PsiQuantityRef
) : PsiScopeProcessor {
    private var result: PsiUID? = null

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is PsiVariables) {
            return checkDecl(element.getUIDs())
        }

        if (element is PsiParameters) {
            return checkDecl(element.getUIDs())
        }

        return true
    }

    private fun checkDecl(entries: Collection<PsiUID>): Boolean {
        result = entries.find { it.name == quantityRef.name }
        if (result != null) {
            return false
        }
        return true
    }

    fun getResult(): PsiUID? {
        return result
    }
}
