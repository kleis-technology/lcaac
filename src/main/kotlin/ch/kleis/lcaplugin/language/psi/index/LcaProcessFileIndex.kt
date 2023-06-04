package ch.kleis.lcaplugin.language.psi.index

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID

class LcaProcessFileIndex {
    companion object {
        val NAME = ID.create<String, Void>("LcaProcessFileIndex[")
        fun findFiles(
            project: Project,
            processName: String,
            scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
        ): Collection<LcaFile> {
            return FileBasedIndex.getInstance().getContainingFiles(
                NAME,
                processName,
                scope
            ).mapNotNull {
                PsiManager.getInstance(project).findFile(it) as? LcaFile
            }
        }
    }
}
