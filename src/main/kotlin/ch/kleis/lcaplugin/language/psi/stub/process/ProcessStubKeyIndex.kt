package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class ProcessStubKeyIndex : StringStubIndexExtension<LcaProcess>() {
    override fun getKey(): StubIndexKey<String, LcaProcess> {
        return LcaStubIndexKeys.PROCESSES
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findProcesses(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
        ): Collection<LcaProcess> =
            StubIndex.getElements(LcaStubIndexKeys.PROCESSES, fqn, project, scope, LcaProcess::class.java)
    }
}
