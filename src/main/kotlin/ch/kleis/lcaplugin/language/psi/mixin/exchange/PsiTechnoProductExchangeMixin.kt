package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductStub
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiTechnoProductExchangeMixin : StubBasedPsiElementBase<OutputProductStub>,
    PsiTechnoProductExchange {
    constructor(node: ASTNode) : super(node)
    constructor(stub: OutputProductStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiTechnoProductExchange>.getName()
    }
}
