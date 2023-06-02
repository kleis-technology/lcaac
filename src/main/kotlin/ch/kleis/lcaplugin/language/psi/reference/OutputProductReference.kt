package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductKeyIndex
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaplugin.language.type_checker.LcaMatchLabelsEvaluator
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class OutputProductReference(
    element: PsiInputProductSpec
) : PsiReferenceBase<PsiInputProductSpec>(element), PsiPolyVariantReference {
    private val project = element.project
    private val file = element.containingFile as LcaFile
    private val pkgName = file.getPackageName()
    private val imports = file.getImports().map { it.name }
    private val allPkgNames = listOf(pkgName).plus(imports)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val candidateFqns = allPkgNames.map {
            "$it.${element.name}"
        }
        val candidateOutputProducts = candidateFqns
            .flatMap { fqn -> OutputProductKeyIndex.findOutputProducts(project, fqn) }
        if (element.getFromProcessConstraint() == null) {
            return candidateOutputProducts
                .map(::PsiElementResolveResult)
                .toTypedArray()
        }

        val processName = element.getFromProcessConstraint()
            ?.processTemplateSpec
            ?.name
        val matchLabels = element.getFromProcessConstraint()
            ?.processTemplateSpec
            ?.getMatchLabels()
            ?.let { LcaMatchLabelsEvaluator().evalOrNull(it) }
            ?: emptyMap()
        return candidateOutputProducts
            .filter {
                val process = it.getContainingProcess()
                (processName == null || process.name == processName)
                    && process.getLabels() == matchLabels
            }
            .map(::PsiElementResolveResult)
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.OUTPUT_PRODUCTS, project)
            .filter { key ->
                allPkgNames.any {
                    allPkgNames.any {
                        val parts = key.split(".")
                        val prefix = parts.take(parts.size - 1).joinToString(".")
                        prefix.startsWith(it)
                    }
                }
            }
            .map { it.split(".").last() }
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
    }
}
