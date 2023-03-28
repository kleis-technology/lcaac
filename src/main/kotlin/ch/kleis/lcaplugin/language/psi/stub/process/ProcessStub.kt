package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.psi.stubs.StubElement

interface ProcessStub : StubElement<PsiProcess> {
    val fqn: String
}
