package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile

class LcaFileCollector(
    private val projectFiles: List<LcaFile>
) {

    fun collect(pkgName: String): List<LcaFile> {
        val result = ArrayList<LcaFile>()
        recursiveCollect(result, emptyList(), pkgName)
        return result
    }

    private fun recursiveCollect(accumulator: ArrayList<LcaFile>, visited: List<String>, pkgName: String) {
        if (visited.contains(pkgName)) {
            return
        }
        val files = projectFiles.filter { it.getPackage().name == pkgName }
        accumulator.addAll(files)

        val imports = files
            .flatMap { it.getImports() }
            .map { it.name }
            .toSet()
        val v = ArrayList(visited)
        for (it in imports) {
            recursiveCollect(accumulator, v, it)
            v.add(it)
        }
    }
}
