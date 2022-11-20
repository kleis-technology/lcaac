package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.SubstanceKeyIndex
import com.intellij.openapi.project.Project


interface SearchTrait {

    fun findProductExchanges(project: Project, name: String): Collection<PsiProductExchange> =
        ProductExchangeKeyIndex.findProductExchanges(project, name)

    fun findSubstances(project: Project, name: String): Collection<PsiSubstance> =
        SubstanceKeyIndex.findSubstances(project, name)

}
