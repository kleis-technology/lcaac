package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.PsiExchangeElement
import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.github.albanseurat.lcaplugin.psi.LcaElementType
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProductStubImpl(parent: StubElement<PsiProductElement>, override val productName: String?) :
    StubBase<PsiProductElement>(parent, LcaTypes.PRODUCT_EXCHANGE as IStubElementType<*, *>), ProductStub {

}