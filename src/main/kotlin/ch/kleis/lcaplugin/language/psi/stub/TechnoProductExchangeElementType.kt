package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.impl.LcaTechnoProductExchangeImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class TechnoProductExchangeElementType(debugName: String) : ILightStubElementType<
        TechnoProductExchangeStub,
        PsiTechnoProductExchange>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): TechnoProductExchangeStub {
        return TechnoProductExchangeStubImpl(parentStub as StubElement<PsiTechnoProductExchange>, dataStream.readNameString()!!)
    }

    override fun createStub(
        tree: LighterAST,
        node: LighterASTNode,
        parentStub: StubElement<*>
    ): TechnoProductExchangeStub {
        val refNode = LightTreeUtil.firstChildOfType(tree, node, LcaTypes.PRODUCT_REF) as LighterASTTokenNode
        val keyNode = LightTreeUtil.firstChildOfType(tree, refNode, LcaTypes.UID) as LighterASTTokenNode
        return TechnoProductExchangeStubImpl(
            parentStub as StubElement<PsiTechnoProductExchange>,
            tree.charTable.intern(keyNode.text).toString()
        )
    }

    override fun createStub(
        psi: PsiTechnoProductExchange,
        parentStub: StubElement<out PsiElement>?
    ): TechnoProductExchangeStub {
        val uid = psi.getProductRef().getUID().name
        return TechnoProductExchangeStubImpl(
            parentStub as StubElement<PsiTechnoProductExchange>,
            uid,
        )
    }

    override fun createPsi(stub: TechnoProductExchangeStub): PsiTechnoProductExchange {
        return LcaTechnoProductExchangeImpl(stub, this)
    }

    override fun indexStub(stub: TechnoProductExchangeStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, stub.uid)
    }

    override fun serialize(stub: TechnoProductExchangeStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.uid)
    }
}
