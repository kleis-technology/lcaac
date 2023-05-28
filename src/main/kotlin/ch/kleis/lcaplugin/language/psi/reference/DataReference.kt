package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil

class DataReference(
    element: PsiDataRef
) : PsiReferenceBase<PsiDataRef>(element), PsiPolyVariantReference {
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
        val localMatches = resolveElementLocally(
            QuantityRefExactNameMatcherScopeProcessor(element)
        )
        if (localMatches.isNotEmpty()) {
            return localMatches.map { PsiElementResolveResult(it) }.toTypedArray()
        }
        return resolveElementInGlobalAssignments()
            .plus(resolveElementInUnitDefinitions())
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val localDefns: List<Any> = resolveElementLocally(
            QuantityRefCollectorScopeProcessor()
        )
            .mapNotNull { psi ->
                psi.name?.let {
                    LookupElementBuilder.create(it)
                }
            }
        val globalDefns = globalAssignmentRef.variants.toList()
        val unitDefns = unitRef.variants.toList()
        return localDefns.plus(globalDefns).plus(unitDefns).toTypedArray()
    }

    private fun resolveElementLocally(
        resolver: QuantityRefScopeProcessor
    ): Set<PsiNameIdentifierOwner> {
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

    private fun resolveElementInGlobalAssignments(): Set<PsiElement> {
        return globalAssignmentRef.multiResolve(false)
            .mapNotNull { it.element }
            .toSet()
    }

    private fun resolveElementInUnitDefinitions(): Set<PsiElement> {
        return unitRef.multiResolve(false)
            .mapNotNull { it.element }
            .toSet()
    }
}
