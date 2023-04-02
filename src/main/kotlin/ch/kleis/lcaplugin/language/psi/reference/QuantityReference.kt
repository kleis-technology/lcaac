package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil

class QuantityReference(
    element: PsiQuantityRef
) : PsiReferenceBase<PsiQuantityRef>(element), PsiPolyVariantReference {
    private val globalAssignmentRef = GlobalUIDOwnerReference(
        element,
        { project, fqn ->
            GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, fqn)
        },
        { project ->
            StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS, project)
        }
    )
    private val unitRef = GlobalUIDOwnerReference(
        element,
        { project, fqn ->
            UnitKeyIndex.findUnits(project, fqn)
        },
        { project ->
            StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.UNITS, project)
        }
    )

    override fun resolve(): PsiElement? {
        val results = multiResolve(false).mapNotNull { it.element }
        return if (results.size == 1) results.first() else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val localMatches = tryLocally()
        if (localMatches.isNotEmpty()) {
            return localMatches.map { PsiElementResolveResult(it) }.toTypedArray()
        }
        return tryGlobalAssignments()
            .plus(tryUnits())
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    private fun tryLocally(): Set<PsiElement> {
        val resolver = QuantityRefScopeProcessor(element)
        var lastParent: PsiElement? = null
        val parents = PsiTreeUtil.collectParents(element, PsiElement::class.java, false) {
            it is LcaFile
        }
        for (parent in parents) {
            val doBreak = !parent.processDeclarations(resolver, ResolveState.initial(), lastParent, element)
            if (doBreak) {
                break
            }
            lastParent = parent
        }
        return resolver.getResults()
    }

    private fun tryGlobalAssignments(): Set<PsiElement> {
        return globalAssignmentRef.multiResolve(false)
                .mapNotNull { it.element }
            .toSet()
    }

    private fun tryUnits(): Set<PsiElement> {
        return unitRef.multiResolve(false)
            .mapNotNull { it.element }
            .toSet()
    }
}
