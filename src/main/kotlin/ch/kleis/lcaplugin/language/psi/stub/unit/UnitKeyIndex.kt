package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class UnitKeyIndex : StringStubIndexExtension<PsiUnitLiteral>() {
    override fun getKey(): StubIndexKey<String, PsiUnitLiteral> {
        return LcaStubIndexKeys.UNITS
    }

    companion object {
        fun findUnits(
            project: Project,
            target: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ) : Collection<PsiUnitLiteral> = TODO()
    }
}
