package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.psi.stubs.StubIndex

class UnitReference(
    element: PsiUnitRef,
) : GlobalUIDOwnerReference<PsiUnitRef, PsiUnitDefinition>(
    element,
    { project, fqn ->
        UnitKeyIndex.findUnits(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.UNITS, project)
    }
)
