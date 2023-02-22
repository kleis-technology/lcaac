package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.Beta

class LinkerException(msg: String): Exception(msg)

data class Linker(
    private val entryPoint: EntryPoint,
    private val dependencies: List<Package>,
) {
    private val beta = Beta()
    private val pkgMap = dependencies.plus(entryPoint.pkg)
        .associateBy { it.name }

    fun link(): Pair<Expression, Environment> {
        val environment = Environment()
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
        result: Environment,
    ) {
        val substitutions = mkSubstitutions(pkg)
        pkg.definitions.forEach { key, value ->
            result[fqn(pkg.name, key)] =
                beta.substitute(
                    substitutions,
                    value
                )
        }
    }

    private fun mkSubstitutions(pkg: Package): List<Pair<String, EVar>> {
        val wildcards = pkg.imports
            .filterIsInstance<ImportWildCard>()
            .flatMap {
                val p = pkgMap[it.pkgName]!!
                p.definitions.keys().map { key ->
                    Pair(key, EVar(fqn(p.name, key)))
                }
            }
        val wildcardKeys = wildcards.map { it.first }.toSet()

        val externals = pkg.imports
            .filterIsInstance<ImportSymbol>()
            .map { Pair(it.name, EVar(fqn(it.pkgName, it.name))) }
        val externalKeys = externals.map { it.first }.toSet()

        val locals = pkg.definitions.keys()
            .map { Pair(it, EVar(fqn(pkg.name, it))) }
        val localKeys = locals.map { it.first }.toSet()

        val conflicts = (wildcardKeys.intersect(localKeys))
            .union(wildcardKeys.intersect(externalKeys))
            .union(externalKeys.intersect(localKeys))
        if (conflicts.isNotEmpty()) {
            throw LinkerException("conflicting symbols $conflicts")
        }

        return wildcards
            .plus(externals)
            .plus(locals)
    }

    private fun fqn(name: String, key: String): String {
        return "$name.$key"
    }
}
