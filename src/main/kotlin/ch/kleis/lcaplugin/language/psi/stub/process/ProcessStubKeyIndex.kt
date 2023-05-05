package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class ProcessStubKeyIndex : StringStubIndexExtension<PsiProcess>() {
    override fun getKey(): StubIndexKey<String, PsiProcess> {
        return LcaStubIndexKeys.PROCESSES
    }

    companion object {
        fun findProcesses(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
        ): Collection<PsiProcess> =
            StubIndex.getElements(LcaStubIndexKeys.PROCESSES, fqn, project, scope, PsiProcess::class.java)
    }
}
