package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.type.Substance
import com.github.albanseurat.lcaplugin.language.psi.stub.SubstanceStub
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType

abstract class SubstanceMixin : StubBasedPsiElementBase<SubstanceStub>,
    IdentifiableTrait, Substance {

    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    // TODO : replace with a better handline (IDENTIFIER OR STRING LITERAL)
    override fun getName(): String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()


}