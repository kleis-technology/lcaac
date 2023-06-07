package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaplugin.psi.LcaLabels
import ch.kleis.lcaplugin.psi.LcaParams
import ch.kleis.lcaplugin.psi.LcaVariables
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

interface DataRefScopeProcessor : PsiScopeProcessor {
    fun getResults(): Set<PsiNameIdentifierOwner>
}

class DataRefCollectorScopeProcessor : DataRefScopeProcessor {
    private var results: MutableSet<PsiNameIdentifierOwner> = mutableSetOf()
    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is LcaVariables) {
            results.addAll(element.assignmentList)
        }

        if (element is LcaParams) {
            results.addAll(element.assignmentList)
        }

        if (element is LcaLabels) {
            results.addAll(element.labelAssignmentList)
        }
        return true
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}

class DataRefExactNameMatcherScopeProcessor(
    private val dataRef: PsiDataRef
) : DataRefScopeProcessor {
    private var results: Set<PsiNameIdentifierOwner> = emptySet()

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is LcaVariables) {
            return checkDecl(element.assignmentList)
        }

        if (element is LcaParams) {
            return checkDecl(element.assignmentList)
        }

        if (element is LcaLabels) {
            return checkDecl(element.labelAssignmentList)
        }

        return true
    }

    private fun checkDecl(entries: Collection<PsiNameIdentifierOwner>): Boolean {
        results = entries.filter { it.name == dataRef.name }.toSet()
        return results.isEmpty()
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}
