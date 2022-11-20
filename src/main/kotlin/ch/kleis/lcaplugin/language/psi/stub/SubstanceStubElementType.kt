package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.PsiSubstanceId
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.impl.LcaSubstanceImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class SubstanceStubElementType(debugName: String) : ILightStubElementType<SubstanceStub,
        PsiSubstance>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SubstanceStub {
        return SubstanceStubImpl(parentStub as StubElement<PsiSubstance>, dataStream.readNameString()!!, dataStream.readNameString()!!, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): SubstanceStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, LcaTypes.SUBSTANCE_ID) as LighterASTTokenNode
        return SubstanceStubImpl(
            parentStub as StubElement<PsiSubstance>,
            tree.charTable.intern(keyNode.text).toString(), "", ""
        );
    }

    override fun createStub(psi: PsiSubstance, parentStub: StubElement<out PsiElement>?): SubstanceStub {
        val substanceId  = psi.nameIdentifier as PsiSubstanceId
        val substanceName = substanceId.getSubstance().name!!
        val compartment = substanceId.getCompartment()?.name
        val subCompartment = substanceId.getSubcompartment()?.name
        return SubstanceStubImpl(parentStub as StubElement<PsiSubstance>,
            substanceName,
            compartment,
            subCompartment,
        )
    }

    override fun createPsi(stub: SubstanceStub): PsiSubstance {
        return LcaSubstanceImpl(stub, this);
    }

    override fun indexStub(stub: SubstanceStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.SUBSTANCES, stub.getCompositeName());
    }

    override fun serialize(stub: SubstanceStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.substanceName);
        dataStream.writeName(stub.compartment);
        if(stub.subCompartment != null) {
            dataStream.writeName(stub.subCompartment);
        }
    }

}
