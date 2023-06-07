package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.psi.stubs.StubElement

interface ProcessStub : StubElement<LcaProcess> {
    val key: ProcessKey
}
