package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import com.intellij.psi.stubs.StubIndex

class ProcessReference(
    element: PsiProcessTemplateRef
) : GlobalUIDOwnerReference<PsiProcessTemplateRef, PsiProcess>(
    element,
    { project, fqn ->
        ProcessStubKeyIndex.findProcesses(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.PROCESSES, project)
    }
)
