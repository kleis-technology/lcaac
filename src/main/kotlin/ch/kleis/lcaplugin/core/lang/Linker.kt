package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.Beta

data class Linker(
    private val entryPoint: EntryPoint,
    private val dependencies: Set<Package>,
) {
    private val beta = Beta()
    private val pkgMap = dependencies.plus(entryPoint.pkg)
        .associateBy { it.name }

    fun link(): Pair<Expression, Environment> {
        val environment = MutableEnvironment()
        dependencies.forEach { pkg ->
            registerFullyQualifiedDefinitions(pkg, environment)
        }
        registerFullyQualifiedDefinitions(entryPoint.pkg, environment)
        return Pair(
            environment[entryPoint.fqn()]!!,
            environment,
        )
    }

    private fun registerFullyQualifiedDefinitions(
        pkg: Package,
        result: MutableEnvironment,
    ) {
        val substitutions = mkSubstitutions(pkg)
        pkg.definitions.forEach { definition ->
            result[fqn(pkg.name, definition.key)] =
                beta.substitute(
                    substitutions,
                    definition.value
                )
        }
    }

    private fun mkSubstitutions(pkg: Package): List<Pair<String, EVar>> {
        val wildcards = pkg.imports
            .filterIsInstance<ImportWildCard>()
            .flatMap {
                val p = pkgMap[it.pkgName]!!
                p.definitions.keys.map { key ->
                    Pair(key, EVar(fqn(p.name, key)))
                }
            }
        val externals = pkg.imports
            .filterIsInstance<ImportSymbol>()
            .map { Pair(it.name, EVar(fqn(it.pkgName, it.name))) }
        val locals = pkg.definitions.keys
            .map { Pair(it, EVar(fqn(pkg.name, it))) }
        return wildcards
            .plus(externals)
            .plus(locals)
    }

    private fun fqn(name: String, key: String): String {
        return "$name.$key"
    }
}
