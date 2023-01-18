package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiProductExchangeMixin : StubBasedPsiElementBase<ProductExchangeStub>, PsiProductExchange {

    constructor(node: ASTNode) : super(node)
    constructor(stub: ProductExchangeStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? = super<PsiProductExchange>.getName()

    override fun toString(): String {
        return "Product(${this.name})"
    }
}
