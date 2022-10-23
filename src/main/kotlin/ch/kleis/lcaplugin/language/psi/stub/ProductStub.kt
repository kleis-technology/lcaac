package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.type.Product
import com.intellij.psi.stubs.StubElement

interface ProductStub : StubElement<Product> {

    val productName : String?
}
