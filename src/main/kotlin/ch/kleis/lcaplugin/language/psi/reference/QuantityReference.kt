package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil

class QuantityReference(
    element: PsiQuantityRef
) : PsiReferenceBase<PsiQuantityRef>(element) {
    private val globalUIDOwnerReference = GlobalUIDOwnerReference(
        element,
        { project, fqn ->
            GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, fqn)
        },
        { project ->
            StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS, project)
        }
    )

    override fun resolve(): PsiElement? {
        return tryLocally()
            ?: tryGlobally()
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

    private fun tryGlobally(): PsiElement? {
        return globalUIDOwnerReference.resolve()
                ?.let { it as PsiGlobalAssignment }
    }
}
