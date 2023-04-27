package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class LcaFileCollector(
    private val refFileResolver: (PsiElement) -> LcaFile? = { ref ->
        ref.reference?.resolve()?.containingFile as LcaFile?
    },
) {

    companion object {
        private val LOG = Logger.getInstance(LcaFileCollector::class.java)
    }

    fun collect(file: LcaFile): Sequence<LcaFile> { // TODO Collect Symbols instead of files ?
        val result = HashMap<String, LcaFile>()
        LOG.info("Start recursive collect")
        recursiveCollect(result, mutableMapOf(file.virtualFile.path to file))
        LOG.info("End recursive collect, found ${result.size} entries")
        return result.values.asSequence()
    }



    private tailrec fun recursiveCollect(
        accumulator: MutableMap<String, LcaFile>,
        toVisit: MutableMap<String, LcaFile>
    ) {
        if (toVisit.isEmpty()) return
        val path = toVisit.keys.first()
        val file = toVisit.remove(path)!!
        val maybeVisited = accumulator[path]
        if (maybeVisited == null) {
            accumulator[path] = file
            val dependencies = dependenciesOf(file)
            val newDependencies = dependencies.asSequence()
                .map { it.virtualFile.path to it }
                .filter { (p, _) -> !accumulator.containsKey(p) }
                .associateTo(toVisit) { it }
            recursiveCollect(accumulator, newDependencies)
        } else {
            recursiveCollect(accumulator, toVisit)
        }
    }

    private fun dependenciesOf(file: LcaFile): Set<LcaFile> {
        return allReferences(file).mapNotNull { refFileResolver(it) }.toSet()
    }

    private fun allReferences(file: LcaFile): List<PsiElement> {
        return PsiTreeUtil.findChildrenOfAnyType(
            file,
            PsiSubstanceRef::class.java,
            PsiQuantityRef::class.java,
            PsiProductRef::class.java,
            PsiProcessTemplateRef::class.java,
            PsiUnitRef::class.java
        ).toList()
    }
}
