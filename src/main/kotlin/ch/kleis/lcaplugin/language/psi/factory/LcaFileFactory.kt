package ch.kleis.lcaplugin.language.psi.factory

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory

class LcaFileFactory(
    private val project : Project,
) {
    fun createFile(content: String) : LcaFile {
        return PsiFileFactory.getInstance(project).createFileFromText(
            "__dummy__.${LcaFileType.INSTANCE.defaultExtension}",
            LcaFileType.INSTANCE,
            content
        ) as LcaFile
    }
}
