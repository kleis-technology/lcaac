package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

sealed class PsiUIDOwnerManipulator<E : PsiUIDOwner> : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E {
        newContent?.let { element.setName(it) }
        return element
    }
}

class PsiQuantityRefManipulator() : PsiUIDOwnerManipulator<PsiQuantityRef>()
class PsiSubstanceRefManipulator() : PsiUIDOwnerManipulator<PsiSubstanceRef>()
class PsiProductRefManipulator() : PsiUIDOwnerManipulator<PsiProductRef>()
class PsiProcessTemplateRefManipulator() : PsiUIDOwnerManipulator<PsiProcessTemplateRef>()
class PsiUnitRefManipulator(): PsiUIDOwnerManipulator<PsiUnitRef>()

class PsiTechnoProductExchangeManipulator() : AbstractElementManipulator<PsiTechnoProductExchange>() {
    override fun handleContentChange(
        element: PsiTechnoProductExchange,
        range: TextRange,
        newContent: String?
    ): PsiTechnoProductExchange? {
        newContent?.let { element.getProductRef().setName(it) }
        return element
    }
}
