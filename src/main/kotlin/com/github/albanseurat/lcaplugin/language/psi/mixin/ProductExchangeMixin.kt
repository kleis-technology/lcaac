package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.github.albanseurat.lcaplugin.language.psi.PsiUnitElement
import com.github.albanseurat.lcaplugin.language.psi.stub.ProductStub
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

abstract class ProductExchangeMixin : StubBasedPsiElementBase<ProductStub>,
    IdentifiableTrait, PsiProductElement {

    constructor(node: ASTNode) : super(node)
    constructor(stub: ProductStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()


    override fun getUnitElement(): PsiUnitElement? {
        return getNode().findChildByType(LcaTypes.QUANTITY)?.psi as PsiUnitElement?
    }


}