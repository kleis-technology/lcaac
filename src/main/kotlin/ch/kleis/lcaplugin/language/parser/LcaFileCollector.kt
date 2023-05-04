package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.*
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

    private val guard = CacheGuard()

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
        return guard.guard { el: PsiElement -> refFileResolver(el) }(element)
    }

    private fun allReferences(file: LcaFile): Sequence<PsiElement> {
        return PsiTreeUtil.findChildrenOfAnyType(
            file,
            PsiSubstanceRef::class.java,
            PsiQuantityRef::class.java,
            PsiProductRef::class.java,
            PsiProcessTemplateRef::class.java,
            PsiUnitRef::class.java
        ).asSequence()
    }
}

private class CacheGuard {
    private val visited = HashSet<String>()

    fun <E : PsiElement, R> guard(fn: (E) -> R?): (E) -> R? {
        return { element ->
            val key = when (element) {
                is PsiUIDOwner -> element.getFullyQualifiedName()
                else -> element.toString()
            }
            if (!visited.contains(key)) {
                visited.add(key)
                fn(element)
            } else {
                null
            }
        }
    }
}