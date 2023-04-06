package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiArgument
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import com.intellij.psi.*

class ParameterReference(
    element: PsiParameterRef
) : PsiReferenceBase<PsiParameterRef>(element), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results.first().element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val argument = element.parent
        if (argument !is PsiArgument) {
            return emptyArray()
        }
        val fromProcessConstraint = argument.parent
        if (fromProcessConstraint !is PsiFromProcessConstraint) {
            return emptyArray()
        }
        return fromProcessConstraint.getProcessTemplateRef().reference.multiResolve(false)
            .mapNotNull { it.element }
            .filterIsInstance<PsiProcess>()
            .flatMap { process -> findAssignments(process) }
            .toTypedArray()
    }

    private fun findAssignments(psiElement: PsiElement): List<PsiElementResolveResult> {
        if (psiElement !is PsiProcess) {
            return emptyList()
        }
        return psiElement.getPsiParametersBlocks()
            .flatMap { filterAndMap(it) }
    }

    private fun filterAndMap(parameters: PsiParameters): List<PsiElementResolveResult> {
        return parameters.getAssignments()
            .mapNotNull { assignment ->
                assignment
                    .takeIf { it.getQuantityRef().name == element.name }
                    ?.let { PsiElementResolveResult(it) }
            }
    }
}
