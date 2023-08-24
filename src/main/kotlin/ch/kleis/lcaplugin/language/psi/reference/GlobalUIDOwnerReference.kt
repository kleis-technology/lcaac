package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.*

open class GlobalUIDOwnerReference<R : PsiUIDOwner, T : PsiElement>(
    element: R,
    private val findTargets: (Project, String) -> Collection<T>,
    private val getAllKeys: (Project) -> Collection<String>,
) : PsiReferenceBase<R>(element), PsiPolyVariantReference {
    private val file = element.containingFile as LcaFile
    private val pkgName = file.getPackageName()
    private val imports = file.getImportNames()
    private val allPkgNames = listOf(pkgName).plus(imports)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val candidateFqns = allPkgNames
            .map { "$it.${element.name}" }
        return candidateFqns
            .flatMap { fqn ->
                findTargets(element.project, fqn)
            }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return getAllKeys(element.project)
            .filter { key ->
                allPkgNames.any {
                    val parts = key.split(".")
                    val prefix = parts.take(parts.size - 1).joinToString(".")
                    prefix.startsWith(it)
                }
            }
            .map { it.split(".").last() }
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
    }
}
