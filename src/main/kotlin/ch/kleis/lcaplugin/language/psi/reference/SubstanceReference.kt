package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import com.intellij.psi.stubs.StubIndex

class SubstanceReference(
    element: PsiSubstanceRef
) : GlobalUIDOwnerReference<PsiSubstanceRef, PsiSubstance>(
    element,
    { project, fqn ->
        SubstanceKeyIndex.findSubstances(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.SUBSTANCES, project)
    }
)
