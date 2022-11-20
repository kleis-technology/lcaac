package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.ProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiUnitElement
import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType

abstract class ProductExchangeMixin : StubBasedPsiElementBase<ProductExchangeStub>,
    IdentifiableTrait, ProductExchange {

    constructor(node: ASTNode) : super(node)
    constructor(stub: ProductExchangeStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()


    override fun getUnitElement(): PsiUnitElement? {
        return getNode().findChildByType(ch.kleis.lcaplugin.psi.LcaTypes.UNIT)?.psi as PsiUnitElement?
    }

    override fun toString(): String {
        return "Product(${this.name})"
    }


}
