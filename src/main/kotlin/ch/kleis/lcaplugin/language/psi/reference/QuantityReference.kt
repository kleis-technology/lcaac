package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil

class QuantityReference(
    element: PsiQuantityRef
) : PsiReferenceBase<PsiQuantityRef>(element) {
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
        return tryLocally()
            ?: tryGlobalAssignments()
            ?: tryUnits()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return tryLocally()?.let { arrayOf(PsiElementResolveResult(it)) }
            ?: globalAssignmentRef.multiResolve(false).plus(unitRef.multiResolve(false))
            ?: emptyArray()
    }

    private fun tryLocally(): PsiElement? {
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
        return resolver.getResult()
    }

    private fun tryGlobalAssignments(): PsiElement? {
        return globalAssignmentRef.resolve()
                ?.let { it as PsiGlobalAssignment }
    }

    private fun tryUnits(): PsiElement? {
        return unitRef.resolve()
            ?.let { it as PsiUnitDefinition }
    }
}
