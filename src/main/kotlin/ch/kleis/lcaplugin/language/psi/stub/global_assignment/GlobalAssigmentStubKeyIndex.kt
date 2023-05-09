package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class GlobalAssigmentStubKeyIndex : StringStubIndexExtension<PsiGlobalAssignment>() {
    override fun getKey(): StubIndexKey<String, PsiGlobalAssignment> {
        return LcaStubIndexKeys.GLOBAL_ASSIGNMENTS
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findGlobalAssignments(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
        ): Collection<PsiGlobalAssignment> =
            StubIndex.getElements(
                LcaStubIndexKeys.GLOBAL_ASSIGNMENTS,
                fqn,
                project,
                scope,
                PsiGlobalAssignment::class.java
            )
    }
}
