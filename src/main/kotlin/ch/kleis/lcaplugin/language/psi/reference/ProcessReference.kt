package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateSpec
import com.intellij.psi.stubs.StubIndex

class ProcessReference(
    element: PsiProcessTemplateSpec
) : GlobalUIDOwnerReference<PsiProcessTemplateSpec, PsiProcess>(
    element,
    { project, fqn ->
        ProcessStubKeyIndex.findProcesses(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.PROCESSES, project)
    }
)
