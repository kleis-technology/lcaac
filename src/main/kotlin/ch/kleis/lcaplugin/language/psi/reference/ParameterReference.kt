package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiArgument
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*

class ParameterReference(
    element: PsiParameterRef
) : PsiReferenceBase<PsiParameterRef>(element), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results.first().element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return resolveProcess()
            ?.let { process -> findParameters(process) }
            ?.toTypedArray()
            ?: emptyArray()
    }

    private fun findContainingArgument(): PsiArgument? {
        val argument = element.parent
        if (argument !is PsiArgument) {
            return null
        }
        return argument
    }

    private fun findContainingFromProcessConstraint(): PsiFromProcessConstraint? {
        val argument = findContainingArgument() ?: return null
        val fromProcessConstraint = argument.parent
        if (fromProcessConstraint !is PsiFromProcessConstraint) {
            return null
        }
        return fromProcessConstraint
    }

    private fun findTemplateRef(): PsiProcessTemplateRef? {
        return findContainingFromProcessConstraint()?.getProcessTemplateRef()
    }

    private fun resolveProcess(): PsiProcess? {
        return findTemplateRef()?.reference?.resolve() as PsiProcess?
    }

    private fun findParameters(psiProcess: PsiProcess): List<PsiElementResolveResult> {
        return psiProcess.getPsiParametersBlocks()
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

    override fun getVariants(): Array<Any> {
        return resolveProcess()
            ?.getParameters()
            ?.keys
            ?.map { LookupElementBuilder.create(it) }
            ?.toTypedArray()
            ?: emptyArray()
    }
}
