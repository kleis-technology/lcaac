package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange.TechnoProductExchangeKeyIndex
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.psi.stubs.StubIndex

class ProductReference(
    element: PsiProductRef
) : GlobalUIDOwnerReference<PsiProductRef, PsiTechnoProductExchange>(
    element,
    { project, fqn ->
        TechnoProductExchangeKeyIndex.findTechnoProductExchanges(project, fqn)
    },
    { project ->
        StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, project)
    }
)
