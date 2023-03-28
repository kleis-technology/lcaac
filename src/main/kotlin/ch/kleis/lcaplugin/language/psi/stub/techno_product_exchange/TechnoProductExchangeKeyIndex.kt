package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class TechnoProductExchangeKeyIndex : StringStubIndexExtension<PsiTechnoProductExchange>() {
    override fun getKey(): StubIndexKey<String, PsiTechnoProductExchange> {
        return LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES
    }

    companion object {
        fun findTechnoProductExchanges(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiTechnoProductExchange> =
            StubIndex.getElements(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, fqn, project, scope, PsiTechnoProductExchange::class.java)
    }
}
