package ch.kleis.lcaplugin.language.psi.index

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.stubIndexVersion
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

class LcaProcessFileIndexExtension() : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> {
        return LcaProcessFileIndex.NAME
    }

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer<String, Void, FileContent> { fileContent ->
            doIndex(fileContent)
        }
    }

    private fun doIndex(fileContent: FileContent): Map<String, Void?> {
        val lcaFile = fileContent.psiFile as? LcaFile ?: return emptyMap()
        return lcaFile.getProcesses()
            .associate { it.name to null }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getVersion(): Int {
        return stubIndexVersion
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(LcaFileType.INSTANCE)
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }
}
