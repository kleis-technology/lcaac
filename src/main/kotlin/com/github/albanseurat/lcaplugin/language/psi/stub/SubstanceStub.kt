package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.Product
import com.github.albanseurat.lcaplugin.language.psi.Substance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<Substance> {

    val substanceName : String?
}