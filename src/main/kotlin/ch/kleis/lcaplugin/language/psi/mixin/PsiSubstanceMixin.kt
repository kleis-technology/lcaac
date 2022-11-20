package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.IStubElementType

abstract class PsiSubstanceMixin : PsiSubstance, StubBasedPsiElementBase<SubstanceStub> {

    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName() : String? {
        return (nameIdentifier as PsiNamedElement?)?.name
    }

    override fun setName(name: String): PsiElement {
        throw NotImplementedError()
    }

    override fun getNameIdentifier(): PsiElement? {
        return node.findChildByType(LcaTypes.SUBSTANCE_ID)?.psi
    }

    override fun getUnitElement(): PsiUnit {
        val substanceBody = node.findChildByType(LcaTypes.SUBSTANCE_BODY)
        val unitType = substanceBody?.findChildByType(LcaTypes.UNIT_TYPE)
        return unitType?.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
            ?: throw IllegalStateException()
    }

    override fun toString(): String {
        return "Substance(${this.name})"
    }

}
