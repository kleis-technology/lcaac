package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaplugin.psi.LcaAliasForField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.util.PsiTreeUtil

enum class UnitDefinitionType {
    LITERAL, ALIAS
}

interface PsiUnitDefinition : PsiNameIdentifierOwner, StubBasedPsiElement<UnitStub> {
    fun getUnitRef(): PsiDataRef {
        return node.findChildByType(LcaTypes.DATA_REF)?.psi as PsiDataRef
    }

    override fun getName(): String {
        return getUnitRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getUnitRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getUnitRef().name = name
        return this
    }

    fun getSymbolField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.SYMBOL_FIELD)?.psi as PsiStringLiteralField
    }

    fun getDimensionField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.DIM_FIELD)?.psi as PsiStringLiteralField
    }

    fun getAliasForField(): LcaAliasForField {
        return PsiTreeUtil.findChildOfType(this, LcaAliasForField::class.java)!!
    }

    fun getType(): UnitDefinitionType {
        return node.findChildByType(LcaTypes.ALIAS_FOR_FIELD)?.let { UnitDefinitionType.ALIAS }
            ?: UnitDefinitionType.LITERAL
    }
}
