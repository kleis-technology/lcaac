package ch.kleis.lcaplugin.project.libraries

import com.intellij.psi.stubs.PrebuiltStubsProvider
import com.intellij.psi.stubs.SerializedStubTree
import com.intellij.util.indexing.FileContent

class SubstanceStubsProvider : PrebuiltStubsProvider {
    override fun findStub(fileContent: FileContent): SerializedStubTree? {
        TODO("Not yet implemented")
    }
}
