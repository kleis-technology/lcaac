package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Product
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProductStubImpl(parent: StubElement<Product>, override val productName: String?) :
    StubBase<Product>(parent, ch.kleis.lcaplugin.psi.LcaTypes.PRODUCT as IStubElementType<*, *>), ProductStub {

}
