package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiSubstanceTypeField : PsiElement {
    fun getValue(): String {
        return listOfNotNull(
            node.findChildByType(LcaTypes.TYPE_EMISSION_KEYWORD),
            node.findChildByType(LcaTypes.TYPE_RESOURCE_KEYWORD),
            node.findChildByType(LcaTypes.TYPE_LAND_USE_KEYWORD)
        )
            .map { it.psi?.text }
            .firstOrNull() ?: ""
    }
}
