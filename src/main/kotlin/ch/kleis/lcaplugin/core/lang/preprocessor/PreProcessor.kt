package ch.kleis.lcaplugin.core.lang.preprocessor

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Renamer
import ch.kleis.lcaplugin.core.lang.expression.*

class PreProcessor(
    private val lookup: (Environment) -> Expression,
    private val pkg: Package,
    private val dependencies: List<Package>,
) {
    private val pkgMap = dependencies.plus(pkg)
        .associateBy { it.name }

    fun prepare(): Program {
        val environment = assemble(pkg, dependencies)
        return Program(
            lookup(environment),
            environment,
        )
    }

    private fun assemble(pkg: Package, deps: List<Package>): Environment {
        val environment = Environment()
        deps.forEach {
            registerDefinitions(it, environment)
        }
        registerDefinitions(pkg, environment)
        return environment
    }

    private fun registerDefinitions(pkg: Package, environment: Environment) {
        listOf(
            Recorder(
                pkgMap,
                { it.environment.quantities.entries() },
                { it is EQuantityRef },
                environment.quantities,
            ),
            Recorder(
                pkgMap,
                { it.environment.processTemplates.entries() },
                { it is ETemplateRef },
                environment.processTemplates,
            ),
            Recorder(
                pkgMap,
                { it.environment.substanceCharacterizations.entries() },
                { it is ESubstanceCharacterizationRef },
                environment.substanceCharacterizations,
            ),
            Recorder(
                pkgMap,
                { it.environment.units.entries() },
                { it is EUnitRef },
                environment.units,
            ),
        ).forEach {
            it.process(pkg)
        }
    }
}

private data class Recorder<E : Expression>(
    val pkgMap: Map<String, Package>,
    val entriesOf: (Package) -> Set<Map.Entry<String, E>>,
    val selector: (RefExpression) -> Boolean,
    val register: Register<E>,
) {
    private val renamer = Renamer(selector)

    fun process(pkg: Package) {
        val substitutions = mkSubstitutions(pkg)
        entriesOf(pkg).forEach {
            register[fqn(pkg.name, it.key)] = renamer.rename(substitutions, it.value) as E
        }
    }

    private fun mkSubstitutions(
        pkg: Package,
    ): List<Pair<String, String>> {
        val wildcards = pkg.imports
            .filterIsInstance<ImportWildCard>()
            .flatMap {
                val p = pkgMap[it.pkgName]!!
                val keys = entriesOf(p).map { entry -> entry.key }
                keys.map { key ->
                    Pair(key, fqn(p.name, key))
                }
            }
        val wildcardKeys = wildcards.map { it.first }.toSet()

        val externals = pkg.imports
            .filterIsInstance<ImportSymbol>()
            .filter {
                val p = pkgMap[it.pkgName]!!
                val keys = entriesOf(p).map { entry -> entry.key }
                keys.contains(it.name)
            }
            .map {
                Pair(it.name, fqn(it.pkgName, it.name))
            }
        val externalKeys = externals.map { it.first }.toSet()

        val locals = entriesOf(pkg).map { entry -> entry.key }
            .map { Pair(it, fqn(pkg.name, it)) }
        val localKeys = locals.map { it.first }.toSet()

        val conflicts = (wildcardKeys.intersect(localKeys))
            .union(wildcardKeys.intersect(externalKeys))
            .union(externalKeys.intersect(localKeys))
        if (conflicts.isNotEmpty()) {
            throw EvaluatorException("conflicting symbols $conflicts")
        }

        return wildcards
            .plus(externals)
            .plus(locals)
    }
}

private fun fqn(name: String, key: String): String {
    return "$name.$key"
}
