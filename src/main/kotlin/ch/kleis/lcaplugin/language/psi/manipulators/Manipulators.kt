package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiOutputProductSpec
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
class PsiInputProductSpecManipulator : PsiUIDOwnerManipulator<PsiInputProductSpec>()
class PsiOutputProductSpecManipulator : PsiUIDOwnerManipulator<PsiOutputProductSpec>()
class PsiSubstanceSpecManipulator : PsiUIDOwnerManipulator<PsiOutputProductSpec>()
class PsiProcessTemplateSpecManipulator : PsiUIDOwnerManipulator<PsiOutputProductSpec>()


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
