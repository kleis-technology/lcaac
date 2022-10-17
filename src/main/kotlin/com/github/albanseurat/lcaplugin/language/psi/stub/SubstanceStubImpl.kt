package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.Product
import com.github.albanseurat.lcaplugin.language.psi.Substance
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class SubstanceStubImpl(parent: StubElement<Substance>, override val substanceName: String?) :
    StubBase<Substance>(parent, LcaTypes.SUBSTANCE as IStubElementType<*, *>), SubstanceStub {

}