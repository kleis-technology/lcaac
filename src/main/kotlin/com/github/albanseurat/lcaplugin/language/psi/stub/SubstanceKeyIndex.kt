package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.Product
import com.github.albanseurat.lcaplugin.language.psi.Substance
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class SubstanceKeyIndex : StringStubIndexExtension<Substance>() {
    override fun getKey(): StubIndexKey<String, Substance> =
        LcaSubIndexKeys.SUBSTANCES

}