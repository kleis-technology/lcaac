package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.psi.LcaGlobalAssignment
import ch.kleis.lcaplugin.psi.impl.LcaGlobalAssignmentImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class GlobalAssignmentStubElementType(debugName: String) :
    ILightStubElementType<GlobalAssignmentStub, PsiGlobalAssignment>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): GlobalAssignmentStub {
        return GlobalAssignmentStubImpl(parentStub as StubElement<PsiGlobalAssignment>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): GlobalAssignmentStub {
        throw UnsupportedOperationException("cannot create process stub from lighter ast node")
    }

    override fun createStub(psi: PsiGlobalAssignment, parentStub: StubElement<out PsiElement>?): GlobalAssignmentStub {
        val fqn = psi.getQuantityRef().getFullyQualifiedName()
        return GlobalAssignmentStubImpl(parentStub as StubElement<PsiGlobalAssignment>, fqn)
    }

    override fun createPsi(stub: GlobalAssignmentStub): PsiGlobalAssignment {
        return LcaGlobalAssignmentImpl(stub, this)
    }

    override fun indexStub(stub: GlobalAssignmentStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS, stub.fqn)
    }

    override fun serialize(stub: GlobalAssignmentStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
