package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef

class ParameterReference(
    element: PsiParameterRef
) : GlobalUIDOwnerReference<PsiParameterRef, PsiAssignment>(
    element,
    { project, fqn -> TODO() },
    { project -> TODO() }
)
