package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.SubstanceStub
import ch.kleis.lcaplugin.language.psi.type.traits.PsiSubstanceIdOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiSubstance :
    PsiSubstanceIdOwner,
    PsiUnitOwner,
    StubBasedPsiElement<SubstanceStub>
