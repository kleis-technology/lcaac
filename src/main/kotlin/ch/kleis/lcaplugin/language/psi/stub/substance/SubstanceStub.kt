package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<PsiSubstance> {
    val key: String
}
