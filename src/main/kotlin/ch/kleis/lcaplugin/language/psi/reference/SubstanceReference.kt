package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.stubs.StubIndex

class SubstanceReference(
    element: PsiSubstanceSpec
) : PsiReferenceBase<PsiSubstanceSpec>(element), PsiPolyVariantReference {
    private val project = element.project
    private val file = element.containingFile as LcaFile
    private val pkgName = file.getPackageName()
    private val imports = file.getImports().map { it.getPackageName() }
    private val allPkgNames = listOf(pkgName).plus(imports)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val type = element.getType()?.value ?: return emptyArray()
        val compartment = element.getCompartmentField()?.getValue() ?: return emptyArray()
        val subCompartment = element.getSubCompartmentField()?.getValue()
        val candidateFqns = allPkgNames.map {
            "$it.${element.getSubstanceRef().name}"
        }
        return candidateFqns
            .flatMap { fqn ->
                SubstanceKeyIndex.findSubstances(
                    project,
                    fqn, type, compartment, subCompartment,
                )
            }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val allKeys = StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.SUBSTANCES, project)
        val filter = allKeys
            .filter { key ->
                allPkgNames.any {
                    val parts = key.split(".")
                    val prefix = parts.take(parts.size - 1).joinToString(".")
                    prefix.startsWith(it)
                }
            }
        val map = filter
            .map { it.split(".").last() }
        val map1 = map
            .map { LookupElementBuilder.create(it) }
        return map1
            .toTypedArray()
    }
}
