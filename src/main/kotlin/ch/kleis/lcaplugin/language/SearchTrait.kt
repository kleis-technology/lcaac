package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.psi.type.ProductExchange
import ch.kleis.lcaplugin.language.psi.type.Substance
import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.SubstanceKeyIndex
import com.intellij.openapi.project.Project


interface SearchTrait {

    fun findProductExchanges(project: Project, name: String): Collection<ProductExchange> =
        ProductExchangeKeyIndex.findProductExchanges(project, name)

    fun findSubstances(project: Project, name: String): Collection<Substance> =
        SubstanceKeyIndex.findSubstances(project, name)

}
