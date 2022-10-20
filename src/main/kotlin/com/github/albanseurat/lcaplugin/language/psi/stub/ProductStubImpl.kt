package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.language.psi.type.Product
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProductStubImpl(parent: StubElement<Product>, override val productName: String?) :
    StubBase<Product>(parent, LcaTypes.PRODUCT as IStubElementType<*, *>), ProductStub {

}