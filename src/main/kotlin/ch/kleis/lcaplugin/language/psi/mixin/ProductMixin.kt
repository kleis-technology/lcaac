package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.Product
import ch.kleis.lcaplugin.language.psi.type.PsiUnitElement
import ch.kleis.lcaplugin.language.psi.stub.ProductStub
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType

abstract class ProductMixin : StubBasedPsiElementBase<ProductStub>,
    IdentifiableTrait, Product {

    constructor(node: ASTNode) : super(node)
    constructor(stub: ProductStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

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
