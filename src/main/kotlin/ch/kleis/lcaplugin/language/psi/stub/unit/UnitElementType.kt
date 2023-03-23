package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.impl.LcaUnitLiteralImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class UnitElementType(debugName: String): ILightStubElementType<
        UnitStub,
        PsiUnitLiteral
>(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): UnitStub {
        return UnitStubImpl(parentStub as StubElement<PsiUnitLiteral>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): UnitStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, LcaTypes.UID) as LighterASTTokenNode
        return UnitStubImpl(
            parentStub as StubElement<PsiUnitLiteral>,
            tree.charTable.intern(keyNode.text).toString(),
        )
    }

    override fun createStub(psi: PsiUnitLiteral, parentStub: StubElement<out PsiElement>?): UnitStub {
        val uid = psi.getUnitRef().getUID().name
        return UnitStubImpl(parentStub as StubElement<PsiUnitLiteral>, uid)
    }

    override fun createPsi(stub: UnitStub): PsiUnitLiteral {
        return LcaUnitLiteralImpl(stub, this)
    }

    override fun indexStub(stub: UnitStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.UNITS, stub.uid)
    }

    override fun serialize(stub: UnitStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.uid)
    }
}
