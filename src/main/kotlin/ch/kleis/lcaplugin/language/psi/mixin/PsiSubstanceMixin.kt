package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.language.reference.ProductExchangeReference
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType

abstract class PsiSubstanceMixin : PsiSubstance, StubBasedPsiElementBase<SubstanceStub> {

    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getUnitElement(): PsiUnit {
        val fieldUnit = node.findChildByType(LcaTypes.FIELD_UNIT)
        return fieldUnit?.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
            ?: throw IllegalStateException()
    }

    override fun getName(): String? = super<PsiSubstance>.getName()

    override fun toString(): String {
        return "Substance(${this.name})"
    }

    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { ProductExchangeReference(this, it.textRangeInParent) }
    }
}
