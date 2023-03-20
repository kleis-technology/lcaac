package ch.kleis.lcaplugin.language.psi.stub

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
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiTechnoProductExchange> =
            StubIndex.getElements(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, target, project, scope, PsiTechnoProductExchange::class.java)
    }
}
