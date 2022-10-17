package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ProductKeyIndex : StringStubIndexExtension<PsiProductElement>() {
    override fun getKey(): StubIndexKey<String, PsiProductElement> =
        LcaSubIndexKeys.PRODUCTS

}