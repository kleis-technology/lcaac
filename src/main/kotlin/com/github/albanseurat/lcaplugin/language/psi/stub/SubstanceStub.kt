package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.type.Substance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<Substance> {

    val substanceName : String?
}