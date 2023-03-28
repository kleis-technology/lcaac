package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.psi.impl.LcaSubstanceImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class SubstanceStubElementType(debugName: String) : ILightStubElementType<SubstanceStub,
        PsiSubstance>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SubstanceStub {
        return SubstanceStubImpl(parentStub as StubElement<PsiSubstance>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): SubstanceStub {
        throw UnsupportedOperationException("cannot create substance stub from lighter ast node")
    }

    override fun createStub(psi: PsiSubstance, parentStub: StubElement<out PsiElement>?): SubstanceStub {
        val fqn = psi.getSubstanceRef().getFullyQualifiedName()
        return SubstanceStubImpl(parentStub as StubElement<PsiSubstance>, fqn)
    }

    override fun createPsi(stub: SubstanceStub): PsiSubstance {
        return LcaSubstanceImpl(stub, this)
    }

    override fun indexStub(stub: SubstanceStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.SUBSTANCES, stub.fqn);
    }

    override fun serialize(stub: SubstanceStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn);
    }
}
