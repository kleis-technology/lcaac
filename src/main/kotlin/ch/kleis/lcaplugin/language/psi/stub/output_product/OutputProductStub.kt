package ch.kleis.lcaplugin.language.psi.stub.output_product

import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import com.intellij.psi.stubs.StubElement

interface OutputProductStub : StubElement<LcaOutputProductSpec> {
    val fqn: String
}
