package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<PsiSubstance> {
    val substanceName: String
    val compartment: String?
    val subCompartment: String?
    fun getCompositeName(): String {
        return listOf(substanceName, compartment, subCompartment)
            .joinToString(", ")
    }
}
