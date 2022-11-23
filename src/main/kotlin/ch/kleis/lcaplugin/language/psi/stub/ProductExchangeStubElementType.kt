package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class ProductExchangeStubElementType(debugName: String) : ILightStubElementType<ProductExchangeStub,
        PsiProductExchange>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProductExchangeStub {
        return ProductExchangeStubImpl(parentStub as StubElement<PsiProductExchange>, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProductExchangeStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, ch.kleis.lcaplugin.psi.LcaTypes.UNIQUE_ID) as LighterASTTokenNode
        return ProductExchangeStubImpl(
            parentStub as StubElement<PsiProductExchange>,
            tree.charTable.intern(keyNode.text).toString()
        );
    }

    override fun createStub(psi: PsiProductExchange, parentStub: StubElement<out PsiElement>?): ProductExchangeStub {
        return ProductExchangeStubImpl(parentStub as StubElement<PsiProductExchange>, psi.name)
    }

    override fun createPsi(stub: ProductExchangeStub): PsiProductExchange {
        return ch.kleis.lcaplugin.psi.impl.LcaProductExchangeImpl(stub, this);
    }

    override fun indexStub(stub: ProductExchangeStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.PRODUCT_EXCHANGES, stub.uniqueId!!);
    }

    override fun serialize(stub: ProductExchangeStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.uniqueId);
    }


}
