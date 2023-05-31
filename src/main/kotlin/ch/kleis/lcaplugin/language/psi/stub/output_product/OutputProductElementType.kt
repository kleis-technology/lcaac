package ch.kleis.lcaplugin.language.psi.stub.output_product

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import ch.kleis.lcaplugin.psi.impl.LcaOutputProductSpecImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class OutputProductElementType(debugName: String) : ILightStubElementType<
    OutputProductStub,
    LcaOutputProductSpec>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): OutputProductStub {
        return OutputProductStubImpl(
            parentStub as StubElement<LcaOutputProductSpec>,
            dataStream.readNameString()!!
        )
    }

    override fun createStub(
        tree: LighterAST,
        node: LighterASTNode,
        parentStub: StubElement<*>
    ): OutputProductStub {
        throw UnsupportedOperationException("cannot create techno product spec stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(
        psi: LcaOutputProductSpec,
        parentStub: StubElement<out PsiElement>?
    ): OutputProductStub {
        val fqn = psi.getFullyQualifiedName()
        return OutputProductStubImpl(
            parentStub as StubElement<LcaOutputProductSpec>,
            fqn,
        )
    }

    override fun createPsi(stub: OutputProductStub): LcaOutputProductSpec {
        return LcaOutputProductSpecImpl(stub, this)
    }

    override fun indexStub(stub: OutputProductStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.OUTPUT_PRODUCTS, stub.fqn)
    }

    override fun serialize(stub: OutputProductStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
