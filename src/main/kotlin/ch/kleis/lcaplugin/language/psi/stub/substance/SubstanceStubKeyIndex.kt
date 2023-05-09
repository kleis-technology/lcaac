package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.AbstractStubIndex
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.io.KeyDescriptor

class SubstanceKeyIndex : AbstractStubIndex<SubstanceKey, PsiSubstance>() {
    override fun getKey(): StubIndexKey<SubstanceKey, PsiSubstance> =
            LcaStubIndexKeys.SUBSTANCES

    override fun getVersion(): Int {
        return System.getProperty("pluginVersion").hashCode()
    }

    override fun getKeyDescriptor(): KeyDescriptor<SubstanceKey> {
        return SubstanceKeyDescriptor.INSTANCE
    }

    companion object {

        fun findSubstances(
                project: Project,
                fqn: String,
                type: String,
                compartment: String,
                subCompartment: String? = null,
                scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<PsiSubstance> =
                StubIndex.getElements(
                        LcaStubIndexKeys.SUBSTANCES,
                        SubstanceKey(fqn, type, compartment, subCompartment),
                        project,
                        scope,
                        PsiSubstance::class.java
                )
    }
}
