package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiVariables
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

interface QuantityRefScopeProcessor : PsiScopeProcessor {
    fun getResults(): Set<PsiNameIdentifierOwner>
}

class QuantityRefCollectorScopeProcessor : QuantityRefScopeProcessor {
    private var results: MutableSet<PsiNameIdentifierOwner> = mutableSetOf()
    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is PsiVariables) {
            recordDefinitions(element.getAssignments())
        }

        if (element is PsiParameters) {
            recordDefinitions(element.getAssignments())
        }

        return true
    }

    private fun recordDefinitions(assignments: Collection<PsiAssignment>) {
        results.addAll(assignments)
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}

class QuantityRefExactNameMatcherScopeProcessor(
    private val quantityRef: PsiQuantityRef
) : QuantityRefScopeProcessor {
    private var results: Set<PsiNameIdentifierOwner> = emptySet()

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is PsiVariables) {
            return checkDecl(element.getAssignments())
        }

        if (element is PsiParameters) {
            return checkDecl(element.getAssignments())
        }

        return true
    }

    private fun checkDecl(entries: Collection<PsiNameIdentifierOwner>): Boolean {
        results = entries.filter { it.name == quantityRef.name }.toSet()
        if (results.isNotEmpty()) {
            return false
        }
        return true
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}
