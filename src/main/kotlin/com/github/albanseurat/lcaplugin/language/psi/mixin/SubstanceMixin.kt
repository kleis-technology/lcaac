package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.type.Substance
import com.github.albanseurat.lcaplugin.language.psi.stub.SubstanceStub
import com.github.albanseurat.lcaplugin.language.psi.type.PsiUnitElement
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class SubstanceMixin : SubstanceIdTrait, StubBasedPsiElementBase<SubstanceStub>,
    Substance {

    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String? = super<SubstanceIdTrait>.getName()
    override fun setName(name: String): PsiElement = super.setName(name)
    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()

    override fun getUnitElement(): PsiUnitElement? {
        return PsiTreeUtil.findChildOfType(this, PsiUnitElement::class.java)
    }
}