package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiSubstance :
    PsiUniqueIdOwner,
    PsiUnitOwner,
    StubBasedPsiElement<SubstanceStub>
