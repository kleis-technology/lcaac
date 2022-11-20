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

    override fun getName() : String? {
        return (nameIdentifier as PsiUniqueId).name
    }

    override fun setName(name: String): PsiElement {
        val identifierNode : ASTNode? = getNode().findChildByType(LcaTypes.UNIQUE_ID)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(getProject())
                .createFileFromText("_Dummy_.${ch.kleis.lcaplugin.LcaFileType.INSTANCE.defaultExtension}", ch.kleis.lcaplugin.LcaFileType.INSTANCE, name)
            getNode().replaceChild(identifierNode, newIdentifier.node)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return node.findChildByType(LcaTypes.UNIQUE_ID)?.psi
    }

    override fun getUnitElement(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
            ?: throw IllegalStateException()
    }

    override fun toString(): String {
        return "Product(${this.name})"
    }


}
