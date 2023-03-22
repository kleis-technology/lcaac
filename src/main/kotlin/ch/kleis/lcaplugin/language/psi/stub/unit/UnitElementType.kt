package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class UnitElementType(debugName: String): ILightStubElementType<
        UnitStub,
        PsiUnitLiteral
>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String {
        TODO("Not yet implemented")
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): UnitStub {
        TODO("Not yet implemented")
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): UnitStub {
        TODO("Not yet implemented")
    }

    override fun createStub(psi: PsiUnitLiteral, parentStub: StubElement<out PsiElement>?): UnitStub {
        TODO("Not yet implemented")
    }

    override fun createPsi(stub: UnitStub): PsiUnitLiteral {
        TODO("Not yet implemented")
    }

    override fun indexStub(stub: UnitStub, sink: IndexSink) {
        TODO("Not yet implemented")
    }

    override fun serialize(stub: UnitStub, dataStream: StubOutputStream) {
        TODO("Not yet implemented")
    }
}
