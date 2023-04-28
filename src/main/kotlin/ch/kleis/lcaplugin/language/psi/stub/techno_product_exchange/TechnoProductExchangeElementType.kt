package ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.psi.impl.LcaTechnoProductExchangeImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class TechnoProductExchangeElementType(debugName: String) : ILightStubElementType<
        TechnoProductExchangeStub,
        PsiTechnoProductExchange>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): TechnoProductExchangeStub {
        return TechnoProductExchangeStubImpl(
            parentStub as StubElement<PsiTechnoProductExchange>,
            dataStream.readNameString()!!
        )
    }

    override fun createStub(
        tree: LighterAST,
        node: LighterASTNode,
        parentStub: StubElement<*>
    ): TechnoProductExchangeStub {
        throw UnsupportedOperationException("cannot create techno product exchange stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(
        psi: PsiTechnoProductExchange,
        parentStub: StubElement<out PsiElement>?
    ): TechnoProductExchangeStub {
        val fqn = psi.getProductRef().getFullyQualifiedName()
        return TechnoProductExchangeStubImpl(
            parentStub as StubElement<PsiTechnoProductExchange>,
            fqn,
        )
    }

    override fun createPsi(stub: TechnoProductExchangeStub): PsiTechnoProductExchange {
        return LcaTechnoProductExchangeImpl(stub, this)
    }

    override fun indexStub(stub: TechnoProductExchangeStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, stub.fqn)
    }

    override fun serialize(stub: TechnoProductExchangeStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
