package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.type.Product
import com.github.albanseurat.lcaplugin.language.psi.type.Substance
import com.intellij.psi.stubs.StubIndexKey

object LcaSubIndexKeys {

    val PRODUCTS : StubIndexKey<String, Product> =
        StubIndexKey.createIndexKey("lca.products")

    val SUBSTANCES : StubIndexKey<String, Substance> =
        StubIndexKey.createIndexKey("lca.substances")
}