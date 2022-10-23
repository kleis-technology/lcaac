package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.psi.type.Product
import ch.kleis.lcaplugin.language.psi.type.Substance
import ch.kleis.lcaplugin.language.psi.stub.ProductKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.SubstanceKeyIndex
import com.intellij.openapi.project.Project


interface SearchTrait {

    fun findProducts(project: Project, name: String): Collection<Product> =
        ProductKeyIndex.findProducts(project, name)

    // TODO : cache results for performance (maybe)
    fun findSubstances(project: Project, name: String): Collection<Substance> =
        SubstanceKeyIndex.findSubstances(project, name)

}
