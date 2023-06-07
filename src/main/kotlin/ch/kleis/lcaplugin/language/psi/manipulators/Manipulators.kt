package ch.kleis.lcaplugin.language.psi.manipulators

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiLabelAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiOutputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiProcessTemplateSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
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
class PsiProductRefManipulator : PsiUIDOwnerManipulator<PsiProductRef>()

sealed class PsiDelegateManipulator<E : PsiElement>(
    private val getter: (E) -> PsiUIDOwner
) : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E? {
        newContent?.let { getter(element).setName(it) }
        return element
    }
}

class PsiSubstanceSpecManipulator : PsiDelegateManipulator<PsiSubstanceSpec>(
    { it.getSubstanceRef() }
)

class PsiInputProductSpecManipulator : PsiDelegateManipulator<PsiInputProductSpec>(
    { it.getProductRef() }
)

class PsiOutputProductSpecManipulator : PsiDelegateManipulator<PsiOutputProductSpec>(
    { it.getProductRef() }
)

class PsiProcessTemplateSpecManipulator : PsiDelegateManipulator<PsiProcessTemplateSpec>(
    { it.getProcessRef() }
)

class PsiLabelAssignmentManipulator : PsiDelegateManipulator<PsiLabelAssignment>(
    { it.getLabelRef() }
)

class PsiGlobalAssignmentManipulator : PsiDelegateManipulator<PsiGlobalAssignment>(
    { it.getDataRef() }
)

class PsiAssignmentManipulator : PsiDelegateManipulator<PsiAssignment>(
    { it.getDataRef() }
)
