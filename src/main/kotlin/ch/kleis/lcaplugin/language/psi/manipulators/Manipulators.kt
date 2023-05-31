package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiProcessTemplateSpec
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

class PsiQuantityRefManipulator : PsiUIDOwnerManipulator<PsiDataRef>()
class PsiSubstanceRefManipulator : PsiUIDOwnerManipulator<PsiSubstanceRef>()
class PsiProcessTemplateRefManipulator : PsiUIDOwnerManipulator<PsiProcessRef>()
class PsiParameterRefManipulator : PsiUIDOwnerManipulator<PsiParameterRef>()

class PsiProcessTemplateSpecManipulator : AbstractElementManipulator<PsiProcessTemplateSpec>() {
    override fun handleContentChange(
        element: PsiProcessTemplateSpec,
        range: TextRange,
        newContent: String?
    ): PsiProcessTemplateSpec {
        newContent?.let { element.setName(it) }
        return element
    }
}

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
        newContent?.let { element.getOutputProductSpec().setName(it) }
        return element
    }
}

class PsiGlobalAssignmentManipulator : AbstractElementManipulator<PsiGlobalAssignment>() {
    override fun handleContentChange(
        element: PsiGlobalAssignment,
        range: TextRange,
        newContent: String?
    ): PsiGlobalAssignment {
        newContent?.let { element.getDataRef().setName(it) }
        return element
    }
}

class PsiAssignmentManipulator : AbstractElementManipulator<PsiAssignment>() {
    override fun handleContentChange(
        element: PsiAssignment,
        range: TextRange,
        newContent: String?
    ): PsiAssignment {
        newContent?.let { element.getDataRef().setName(it) }
        return element
    }
}
