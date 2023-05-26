package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.ref.PsiLabelRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiLabelAssignment : PsiNameIdentifierOwner {
    fun getLabelRef(): PsiLabelRef {
        return PsiTreeUtil.getChildOfType(this, PsiLabelRef::class.java) as PsiLabelRef
    }

    fun getValue(): String {
        return node.findChildByType(LcaTypes.STRING_LITERAL)?.text?.trim('"') as String
    }

    override fun getName(): String {
        return getLabelRef().name
    }

    override fun setName(name: String): PsiElement {
        getLabelRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getLabelRef().nameIdentifier
    }
}
