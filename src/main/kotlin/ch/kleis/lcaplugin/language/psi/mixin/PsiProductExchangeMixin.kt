package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeStub
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.stubs.IStubElementType

abstract class PsiProductExchangeMixin : StubBasedPsiElementBase<ProductExchangeStub>, PsiProductExchange {

    constructor(node: ASTNode) : super(node)
    constructor(stub: ProductExchangeStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? = super<PsiProductExchange>.getName()

    override fun toString(): String {
        return "Product(${this.name})"
    }
}
