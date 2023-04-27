package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.psi.impl.LcaProcessImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class ProcessStubElementType(debugName: String) :
    ILightStubElementType<ProcessStub, PsiProcess>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProcessStub {
        return ProcessStubImpl(parentStub as StubElement<PsiProcess>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProcessStub {
        throw UnsupportedOperationException("cannot create process stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: PsiProcess, parentStub: StubElement<out PsiElement>?): ProcessStub {
        val fqn = psi.getProcessTemplateRef().getFullyQualifiedName()
        return ProcessStubImpl(parentStub as StubElement<PsiProcess>, fqn)
    }

    override fun createPsi(stub: ProcessStub): PsiProcess {
        return LcaProcessImpl(stub, this)
    }

    override fun indexStub(stub: ProcessStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.PROCESSES, stub.fqn);
    }

    override fun serialize(stub: ProcessStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn);
    }
}
