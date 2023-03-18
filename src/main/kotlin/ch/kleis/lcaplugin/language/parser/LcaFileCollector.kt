package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef

class LcaFileCollector(
    private val substanceRefFileResolver: (PsiSubstanceRef) -> LcaFile? = { ref ->
        ref.reference?.resolve()?.containingFile as LcaFile?
    }
) {
    fun collect(file: LcaFile): List<LcaFile> {
        val result = ArrayList<LcaFile>()
        recursiveCollect(result, emptyList(), file)
        return result
    }

    private fun recursiveCollect(accumulator: ArrayList<LcaFile>, visited: List<Int>, file: LcaFile) {
        val h = file.hashCode()
        if (visited.contains(h)) {
            return
        }
        accumulator.add(file)
        val v = ArrayList(visited)
        for (dep in dependenciesOf(file)) {
            recursiveCollect(accumulator, v, dep)
            v.add(dep.hashCode())
        }
    }

    private fun dependenciesOf(file: LcaFile): List<LcaFile> {
        val imports = file.getImports().map { it.getPackageName() }
        return file
            .getProcesses()
            .flatMap {
                it.getEmissions()
                    .plus(it.getResources())
                    .plus(it.getLandUse())
            }
            .map { it.getSubstanceRef() }
            .mapNotNull { substanceRefFileResolver(it) }
            .filter { imports.contains(it.getPackageName()) }
    }
}
