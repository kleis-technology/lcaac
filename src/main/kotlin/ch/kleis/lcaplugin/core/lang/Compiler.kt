package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.prelude.Prelude

data class Compiler(
    private val entryPoint: EntryPoint,
    private val dependencies: Set<Package>,
) {
    fun compile(): Program {
        val deps = dependencies.plus(Prelude.packages.values)
        val (expression, environment) = Linker(entryPoint, deps).link()
        return Program(environment, expression)
    }
}
