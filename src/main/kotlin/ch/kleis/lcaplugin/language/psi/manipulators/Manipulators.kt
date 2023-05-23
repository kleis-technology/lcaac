package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

sealed class PsiUIDOwnerManipulator<E : PsiUIDOwner> : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E {
        newContent?.let { element.setName(it) }
        return element
    }
}

class PsiQuantityRefManipulator : PsiUIDOwnerManipulator<PsiQuantityRef>()
class PsiProductRefManipulator : PsiUIDOwnerManipulator<PsiProductRef>()
class PsiSubstanceRefManipulator : PsiUIDOwnerManipulator<PsiSubstanceRef>()
class PsiProcessTemplateRefManipulator : PsiUIDOwnerManipulator<PsiProcessTemplateRef>()
class PsiParameterRefManipulator : PsiUIDOwnerManipulator<PsiParameterRef>()

class PsiSubstanceSpecManipulator : AbstractElementManipulator<PsiSubstanceSpec>() {
    override fun handleContentChange(
        element: PsiSubstanceSpec,
        range: TextRange,
        newContent: String?
    ): PsiSubstanceSpec {
        newContent?.let { element.setName(it) }
        return element
    }
}

class PsiTechnoProductExchangeManipulator : AbstractElementManipulator<PsiTechnoProductExchange>() {
    override fun handleContentChange(
        element: PsiTechnoProductExchange,
        range: TextRange,
        newContent: String?
    ): PsiTechnoProductExchange {
        newContent?.let { element.getProductRef().setName(it) }
        return element
    }
}

class PsiGlobalAssignmentManipulator : AbstractElementManipulator<PsiGlobalAssignment>() {
    override fun handleContentChange(
        element: PsiGlobalAssignment,
        range: TextRange,
        newContent: String?
    ): PsiGlobalAssignment {
        newContent?.let { element.getQuantityRef().setName(it) }
        return element
    }
}

class PsiAssignmentManipulator : AbstractElementManipulator<PsiAssignment>() {
    override fun handleContentChange(
        element: PsiAssignment,
        range: TextRange,
        newContent: String?
    ): PsiAssignment {
        newContent?.let { element.getQuantityRef().setName(it) }
        return element
    }
}
