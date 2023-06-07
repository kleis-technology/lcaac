package ch.kleis.lcaplugin.language.psi.stub.output_product

import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class OutputProductStubImpl(
    parent: StubElement<LcaOutputProductSpec>,
    override val fqn: String,
) : StubBase<LcaOutputProductSpec>(
    parent,
    LcaTypes.OUTPUT_PRODUCT_SPEC as IStubElementType<out StubElement<*>, *>
), OutputProductStub
