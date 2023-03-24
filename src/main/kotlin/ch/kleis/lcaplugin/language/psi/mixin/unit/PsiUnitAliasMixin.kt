package ch.kleis.lcaplugin.language.psi.mixin.unit

import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitAlias
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiUnitAliasMixin : StubBasedPsiElementBase<UnitStub>, PsiUnitAlias {
    constructor(node: ASTNode) : super(node)
    constructor(stub: UnitStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiUnitAlias>.getName()
    }
}
