package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.collections.immutable.toImmutableList

class LcaFileCollector(
    private val refFileResolver: (PsiElement) -> LcaFile? = { ref ->
        ref.reference?.resolve()?.containingFile as LcaFile?
    },
) {

    companion object {
        private val LOG = Logger.getInstance(LcaFileCollector::class.java)
    }

    fun collect(file: LcaFile): List<LcaFile> { // TODO Collect Symbole instead of files ?
        val result = HashMap<String, LcaFile>()
        LOG.info("Start recursive collect")
        recursiveCollect(result, file)
        LOG.info("End recursive collect")
        return result.values.toImmutableList()
    }

    private fun recursiveCollect(accumulator: MutableMap<String, LcaFile>, file: LcaFile) {
        val k = file.virtualFile.path
        val visited = accumulator[k]
        if (visited == null) {
            accumulator[k] = file
            val deps = dependenciesOf(file)
            for (dep in deps) {
                recursiveCollect(accumulator, dep)
            }
        }
    }

    private fun dependenciesOf(file: LcaFile): Set<LcaFile> {
        return allReferences(file).mapNotNull { refFileResolver(it) }.toSet()
    }

    private fun allReferences(file: LcaFile): List<PsiElement> {
        val substanceRefs = PsiTreeUtil.collectElementsOfType(file, PsiSubstanceRef::class.java)
        val quantityRefs = PsiTreeUtil.collectElementsOfType(file, PsiQuantityRef::class.java)
        val productsRefs = PsiTreeUtil.collectElementsOfType(file, PsiProductRef::class.java)
        val processRefs = PsiTreeUtil.collectElementsOfType(file, PsiProcessTemplateRef::class.java)
        val unitRefs = PsiTreeUtil.collectElementsOfType(file, PsiUnitRef::class.java)
        return listOf(
            processRefs, productsRefs, quantityRefs, substanceRefs, unitRefs
        ).flatten()
    }
}
