package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys.SUBSTANCES
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class SubstanceKeyIndex : StringStubIndexExtension<PsiSubstance>() {
    override fun getKey(): StubIndexKey<String, PsiSubstance> =
        SUBSTANCES

    companion object {

        fun findSubstances(
            project: Project,
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiSubstance> =
            StubIndex.getElements(SUBSTANCES, target, project, scope, PsiSubstance::class.java)

    }
}


