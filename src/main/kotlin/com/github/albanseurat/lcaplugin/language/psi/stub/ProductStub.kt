package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.PsiExchangeElement
import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.intellij.psi.stubs.StubElement

interface ProductStub : StubElement<PsiProductElement> {

    val productName : String?
}