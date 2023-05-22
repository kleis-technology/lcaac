package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.psi.LcaArgument
import ch.kleis.lcaplugin.psi.LcaParams
import ch.kleis.lcaplugin.psi.LcaProcess
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

    private fun findContainingArgument(): LcaArgument? {
        val argument = element.parent
        if (argument !is LcaArgument) {
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

    private fun resolveProcess(): LcaProcess? {
        return findTemplateRef()?.reference?.resolve() as LcaProcess?
    }

    private fun findParameters(process: LcaProcess): List<PsiElementResolveResult> {
        return process.paramsList
            .flatMap { filterAndMap(it) }
    }

    private fun filterAndMap(parameters: LcaParams): List<PsiElementResolveResult> {
        return parameters.assignmentList
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
