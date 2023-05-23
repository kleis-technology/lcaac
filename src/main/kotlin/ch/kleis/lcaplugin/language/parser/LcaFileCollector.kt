package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
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

    private val guard = CacheGuard { el: PsiElement -> refFileResolver(el) }

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
            val newDeps = dependenciesOf(file)
                .map { it.virtualFile.path to it }
                .filter { (p, _) -> !accumulator.containsKey(p) }
                .associateTo(toVisit) { it }
            recursiveCollect(accumulator, newDeps)
        } else {
            recursiveCollect(accumulator, toVisit)
        }
    }

    private fun dependenciesOf(file: LcaFile): Sequence<LcaFile> {
        return allReferences(file).mapNotNull { resolve(it) }
    }

    private fun resolve(element: PsiElement): LcaFile? {
        return guard.guarded(element)
    }

    private fun allReferences(file: LcaFile): Sequence<PsiElement> {
        return PsiTreeUtil.findChildrenOfAnyType(
            file,
            PsiSubstanceSpec::class.java,
            PsiQuantityRef::class.java,
            PsiProductRef::class.java,
            PsiProcessTemplateRef::class.java,
        ).asSequence()
    }
}

private class CacheGuard<E, R>(private val fnToGuard: (E) -> R?) where E : PsiElement {
    private val visited = HashSet<String>()

    val guarded: (E) -> R? = { element ->
        val key = when (element) {
            is PsiUIDOwner -> element.getFullyQualifiedName()
            else -> element.toString()
        }
        if (!visited.contains(key)) {
            visited.add(key)
            fnToGuard(element)
        } else {
            null
        }
    }

}
