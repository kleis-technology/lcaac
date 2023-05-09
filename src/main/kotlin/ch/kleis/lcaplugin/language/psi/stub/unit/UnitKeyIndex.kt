package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class UnitKeyIndex : StringStubIndexExtension<PsiUnitDefinition>() {
    override fun getKey(): StubIndexKey<String, PsiUnitDefinition> {
        return LcaStubIndexKeys.UNITS
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    companion object {
        fun findUnits(
            project: Project,
            fqn: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiUnitDefinition> =
            StubIndex.getElements(LcaStubIndexKeys.UNITS, fqn, project, scope, PsiUnitDefinition::class.java)
    }
}
