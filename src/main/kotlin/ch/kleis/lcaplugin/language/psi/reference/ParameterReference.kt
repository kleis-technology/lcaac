package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.psi.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

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

    private fun findTemplateSpec(): LcaProcessTemplateSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaProcessTemplateSpec::class.java)
    }

    private fun resolveProcess(): LcaProcess? {
        return findTemplateSpec()?.reference?.resolve() as LcaProcess?
    }

    private fun findParameters(process: LcaProcess): List<PsiElementResolveResult> {
        return process.paramsList
            .flatMap { filterAndMap(it) }
    }

    private fun filterAndMap(parameters: LcaParams): List<PsiElementResolveResult> {
        return parameters.assignmentList
            .mapNotNull { assignment ->
                assignment
                    .takeIf { it.getDataRef().name == element.name }
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
