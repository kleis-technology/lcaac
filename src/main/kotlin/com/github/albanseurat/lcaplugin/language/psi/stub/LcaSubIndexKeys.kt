package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.intellij.psi.stubs.StubIndexKey

object LcaSubIndexKeys {

    val PRODUCTS : StubIndexKey<String, PsiProductElement> =
        StubIndexKey.createIndexKey("lca.products")
}