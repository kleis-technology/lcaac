package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.type.spec.PsiProcessTemplateSpec
import ch.kleis.lcaplugin.language.type_checker.LcaMatchLabelsEvaluator
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class ProcessReferenceFromPsiProcessTemplateSpec(
    element: PsiProcessTemplateSpec
) : PsiReferenceBase<PsiProcessTemplateSpec>(element), PsiPolyVariantReference {
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
        val labels = element.getMatchLabels()
            ?.let { LcaMatchLabelsEvaluator().evalOrNull(it) }
            ?: emptyMap()
        return candidateFqns
            .flatMap { fqn -> ProcessStubKeyIndex.findProcesses(project, fqn, labels) }
            .map(::PsiElementResolveResult)
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.PROCESSES, project)
            .filter { key ->
                allPkgNames.any {
                    key.getPackageName().startsWith(it)
                }
            }
            .map { LookupElementBuilder.create(it.getDisplayName()) }
            .toTypedArray()
    }
}
