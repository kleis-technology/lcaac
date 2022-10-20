package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.type.Product
import com.intellij.psi.stubs.StubElement

interface ProductStub : StubElement<Product> {

    val productName : String?
}