package ch.kleis.lcaplugin.language.psi.mixin.spec

import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductStub
import ch.kleis.lcaplugin.language.psi.type.spec.PsiOutputProductSpec
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiOutputProductSpecMixin : StubBasedPsiElementBase<OutputProductStub>, PsiOutputProductSpec {
    constructor(node: ASTNode) : super(node)
    constructor(stub: OutputProductStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiOutputProductSpec>.getName()
    }
}
