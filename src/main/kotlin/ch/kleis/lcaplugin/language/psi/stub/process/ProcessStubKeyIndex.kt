package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.AbstractStubIndex
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.io.KeyDescriptor

class ProcessStubKeyIndex : AbstractStubIndex<ProcessKey, LcaProcess>() {
    override fun getKey(): StubIndexKey<ProcessKey, LcaProcess> {
        return LcaStubIndexKeys.PROCESSES
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    override fun getKeyDescriptor(): KeyDescriptor<ProcessKey> {
        return ProcessKeyDescriptor.INSTANCE
    }

    companion object {
        fun findProcesses(
            project: Project,
            fqn: String,
            labels: Map<String, String> = emptyMap(),
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
        ): Collection<LcaProcess> =
            StubIndex.getElements(LcaStubIndexKeys.PROCESSES,
                ProcessKey(fqn, labels),
                project, scope, LcaProcess::class.java)
    }
}
