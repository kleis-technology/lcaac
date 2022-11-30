package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.compute.urn.Namespace
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class ProductExchangeStubElementType(debugName: String) : ILightStubElementType<ProductExchangeStub,
        PsiProductExchange>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProductExchangeStub {
        return ProductExchangeStubImpl(parentStub as StubElement<PsiProductExchange>, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProductExchangeStub {
        throw UnsupportedOperationException()
    }

    override fun createStub(psi: PsiProductExchange, parentStub: StubElement<out PsiElement>?): ProductExchangeStub {
        val file = psi.containingFile as LcaFile
        val pkg = file.getPackage()
        val parts = pkg.getUrnElement().getParts()
        val uid = (parts + psi.name!!).joinToString(Namespace.SEPARATOR)
        return ProductExchangeStubImpl(parentStub as StubElement<PsiProductExchange>, uid)
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
