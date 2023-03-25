package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiVariables
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

class QuantityRefScopeProcessor(
    private val quantityRef: PsiQuantityRef
) : PsiScopeProcessor {
    private var result: PsiUIDOwner? = null

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is PsiVariables) {
            return checkDecl(element.getAssignments())
        }

        if (element is PsiParameters) {
            return checkDecl(element.getAssignments())
        }

        return true
    }

    private fun checkDecl(entries: Collection<PsiAssignment>): Boolean {
        result = entries.find { it.name == quantityRef.name }
        if (result != null) {
            return false
        }
        return true
    }

    fun getResult(): PsiUIDOwner? {
        return result
    }
}
