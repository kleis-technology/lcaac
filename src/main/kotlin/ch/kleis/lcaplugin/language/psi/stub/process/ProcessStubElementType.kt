package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.impl.LcaProcessImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class ProcessStubElementType(debugName: String) :
    ILightStubElementType<ProcessStub, LcaProcess>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProcessStub {
        val key = ProcessKeyDescriptor.INSTANCE.read(dataStream)
        return ProcessStubImpl(parentStub as StubElement<LcaProcess>, key)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProcessStub {
        throw UnsupportedOperationException("cannot create process stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaProcess, parentStub: StubElement<out PsiElement>?): ProcessStub {
        val fqn = psi.getProcessRef().getFullyQualifiedName()
        val labels = psi.getLabels()
        val key = ProcessKey(fqn, labels)
        return ProcessStubImpl(parentStub as StubElement<LcaProcess>, key)
    }

    override fun createPsi(stub: ProcessStub): LcaProcess {
        return LcaProcessImpl(stub, this)
    }

    override fun indexStub(stub: ProcessStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.PROCESSES, stub.key)
    }

    override fun serialize(stub: ProcessStub, dataStream: StubOutputStream) {
        ProcessKeyDescriptor.INSTANCE.save(dataStream, stub.key)
    }
}
