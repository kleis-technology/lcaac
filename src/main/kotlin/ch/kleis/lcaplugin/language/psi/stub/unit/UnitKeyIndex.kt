package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import ch.kleis.lcaplugin.psi.LcaUnitDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

class UnitKeyIndex : StringStubIndexExtension<LcaUnitDefinition>() {
    override fun getKey(): StubIndexKey<String, LcaUnitDefinition> {
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
        ): Collection<LcaUnitDefinition> =
            StubIndex.getElements(LcaStubIndexKeys.UNITS, fqn, project, scope, LcaUnitDefinition::class.java)
    }
}
