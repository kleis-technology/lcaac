package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.psi.LcaTechnoProductExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class TechnoProductExchangeStubImpl(
    parent: StubElement<LcaTechnoProductExchange>,
    override val fqn: String,
) : StubBase<LcaTechnoProductExchange>(
    parent,
    LcaTypes.TECHNO_PRODUCT_EXCHANGE as IStubElementType<out StubElement<*>, *>
),
    TechnoProductExchangeStub
