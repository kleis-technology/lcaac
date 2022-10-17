package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.Product
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ProductKeyIndex : StringStubIndexExtension<Product>() {
    override fun getKey(): StubIndexKey<String, Product> =
        LcaSubIndexKeys.PRODUCTS

}