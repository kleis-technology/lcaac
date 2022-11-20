package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.ProductExchange
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProductExchangeStubImpl(parent: StubElement<ProductExchange>, override val productName: String?) :
    StubBase<ProductExchange>(parent, ch.kleis.lcaplugin.psi.LcaTypes.PRODUCT_EXCHANGE as IStubElementType<*, *>), ProductExchangeStub {

}
