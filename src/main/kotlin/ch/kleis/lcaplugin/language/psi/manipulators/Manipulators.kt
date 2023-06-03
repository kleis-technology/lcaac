package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiLabelAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiOutputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiProcessTemplateSpec
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiElement

sealed class PsiUIDOwnerManipulator<E : PsiUIDOwner> : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E {
        newContent?.let { element.setName(it) }
        return element
    }
}

class PsiQuantityRefManipulator : PsiUIDOwnerManipulator<PsiDataRef>()
class PsiSubstanceRefManipulator : PsiUIDOwnerManipulator<PsiSubstanceRef>()
class PsiProcessTemplateRefManipulator : PsiUIDOwnerManipulator<PsiProcessRef>()
class PsiLabelRefManipulator : PsiUIDOwnerManipulator<PsiLabelRef>()
class PsiParameterRefManipulator : PsiUIDOwnerManipulator<PsiParameterRef>()
class PsiInputProductSpecManipulator : PsiUIDOwnerManipulator<PsiInputProductSpec>()
class PsiOutputProductSpecManipulator : PsiUIDOwnerManipulator<PsiOutputProductSpec>()
class PsiSubstanceSpecManipulator : PsiUIDOwnerManipulator<PsiOutputProductSpec>()

sealed class PsiDelegateManipulator<E : PsiElement>(
    private val getter: (E) -> PsiUIDOwner
) : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E? {
        newContent?.let { getter(element).setName(it) }
        return element
    }
}

class PsiProcessTemplateSpecManipulator : PsiDelegateManipulator<PsiProcessTemplateSpec>(
    { it.getProcessRef() }
)


class PsiLabelAssignmentManipulator : AbstractElementManipulator<PsiLabelAssignment>() {
    override fun handleContentChange(
        element: PsiLabelAssignment,
        range: TextRange,
        newContent: String?
    ): PsiLabelAssignment {
        newContent?.let { element.getLabelRef().setName(it) }
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
