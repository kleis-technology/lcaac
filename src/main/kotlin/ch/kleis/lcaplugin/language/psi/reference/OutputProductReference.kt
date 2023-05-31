package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductKeyIndex
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiOutputProductSpec
import com.intellij.psi.stubs.StubIndex

class OutputProductReference(
    element: PsiInputProductSpec
) : GlobalUIDOwnerReference<PsiInputProductSpec, PsiOutputProductSpec>(
    element,
    { project, fqn ->
        OutputProductKeyIndex.findOutputProducts(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.OUTPUT_PRODUCTS, project)
    }
)
