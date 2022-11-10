package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Product
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class ProductKeyIndex : StringStubIndexExtension<Product>() {
    override fun getKey(): StubIndexKey<String, Product> =
        LcaSubIndexKeys.PRODUCTS


    companion object {

        fun findProducts(
            project: Project,
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<Product> =
            StubIndex.getElements(LcaSubIndexKeys.PRODUCTS, target, project, scope, Product::class.java)
    }
}
