package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class ProductExchangeKeyIndex : StringStubIndexExtension<PsiProductExchange>() {
    override fun getKey(): StubIndexKey<String, PsiProductExchange> =
        LcaStubIndexKeys.PRODUCT_EXCHANGES


    companion object {

        fun findProductExchanges(
            project: Project,
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiProductExchange> =
            StubIndex.getElements(LcaStubIndexKeys.PRODUCT_EXCHANGES, target, project, scope, PsiProductExchange::class.java)
    }
}
