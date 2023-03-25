package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.psi.impl.LcaUnitDefinitionImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class UnitElementType(debugName: String): ILightStubElementType<
        UnitStub,
        PsiUnitDefinition
>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): UnitStub {
        return UnitStubImpl(parentStub as StubElement<PsiUnitDefinition>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): UnitStub {
        throw UnsupportedOperationException("cannot create unit stub from lighter ast node")
    }

    override fun createStub(psi: PsiUnitDefinition, parentStub: StubElement<out PsiElement>?): UnitStub {
        val fqn = psi.getUnitRef().getFullyQualifiedName()
        return UnitStubImpl(parentStub as StubElement<PsiUnitDefinition>, fqn)
    }

    override fun createPsi(stub: UnitStub): PsiUnitDefinition {
        return LcaUnitDefinitionImpl(stub, this)
    }

    override fun indexStub(stub: UnitStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.UNITS, stub.fqn)
    }

    override fun serialize(stub: UnitStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
