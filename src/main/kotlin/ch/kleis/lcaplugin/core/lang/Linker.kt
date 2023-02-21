package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.Beta

data class Linker(
    private val entryPoint: EntryPoint,
    private val dependencies: Set<Package>,
) {
    private val beta = Beta()
    fun run(): Pair<Expression, Map<String, Expression>> {
        val result = HashMap<String, Expression>()
        dependencies.forEach { pkg ->
            applyUpdate(pkg, result, mkSubstitutions(pkg))
        }
        val substitutions = mkSubstitutions(entryPoint.pkg)
        applyUpdate(entryPoint.pkg, result, substitutions)
        return Pair(
            beta.substitute(substitutions, entryPoint.getExpression()),
            result
        )
    }

    private fun applyUpdate(
        pkg: Package,
        result: HashMap<String, Expression>,
        substitutions: List<Pair<String, EVar>>
    ) {
        pkg.definitions.forEach { definition ->
            result[fqn(pkg.name, definition.key)] = beta.substitute(
                substitutions,
                definition.value
            )
        }
    }

    private fun mkSubstitutions(pkg: Package): List<Pair<String, EVar>> {
        val externals = pkg.imports
            .map { Pair(it.name, EVar(fqn(it.pkgName, it.name))) }
        val locals = pkg.definitions.keys
            .map { Pair(it, EVar(fqn(pkg.name, it))) }
        return externals.plus(locals)
    }

    private fun fqn(name: String, key: String): String {
        return "$name.$key"
    }
}
