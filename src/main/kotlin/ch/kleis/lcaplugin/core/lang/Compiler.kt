package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.prelude.Prelude

data class Compiler(
    private val entryPoint: EntryPoint,
    private val dependencies: Set<Package>,
) {
    fun run(): Program {
        val dep = dependencies.plus(Prelude.packages.values)
        val (expression, environment) = Linker(entryPoint, dep).run()
        return Program(environment, expression)
    }
}
