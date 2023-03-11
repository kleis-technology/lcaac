package ch.kleis.lcaplugin.core.lang.preprocessor

import arrow.core.compose
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.*

class PreProcessor(
    private val pkg: Package,
    private val dependencies: List<Package>,
) {
    fun assemble(): Environment {
        val wildcards = pkg.imports.filterIsInstance<ImportWildCard>().map { it.pkgName }
        val externalSymbols = pkg.imports.filterIsInstance<ImportSymbol>()

        val depEnvs = dependencies.map { dep ->
            val syms = externalSymbols.filter { it.pkgName == dep.name }.map { it.name }
            val filter : (String) -> Boolean = {
                wildcards.contains(dep.name) || syms.contains(it)
            }
            dep.name to Environment(
                products = Register(dep.environment.products.filterKeys { filter(it) }),
                substances = Register(dep.environment.substances.filterKeys { filter(it) }),
                indicators = Register(dep.environment.indicators.filterKeys { filter(it) }),
                quantities = Register(dep.environment.quantities.filterKeys { filter(it) }),
                units = Register(dep.environment.units.filterKeys { filter(it) }),
                processTemplates = Register(dep.environment.processTemplates.filterKeys { filter(it) }),
                substanceCharacterizations = Register(dep.environment.substanceCharacterizations.filterKeys {
                    syms.contains(
                        it
                    )
                }),
            )
        }

        val result = Environment.empty()
        var substitution = Substitution.empty()
        depEnvs.plus(pkg.name to pkg.environment)
            .forEach { (pkgName, pkgEnv) ->
                val s = load(result, pkgName, pkgEnv)
                substitution = substitution.plus(s)
            }

        return substitution.apply(result)
    }

    private fun load(environment: Environment, pkgName: String, pkgEnv: Environment): Substitution {
        val products = HashMap<String, String>()
        pkgEnv.products.entries.forEach {
            environment.products[fqn(pkgName, it.key)] = it.value
            products[it.key] = fqn(pkgName, it.key)
        }

        val substances = HashMap<String, String>()
        pkgEnv.substances.entries.forEach {
            environment.substances[fqn(pkgName, it.key)] = it.value
            substances[it.key] = fqn(pkgName, it.key)
        }

        val indicators = HashMap<String, String>()
        pkgEnv.indicators.entries.forEach {
            environment.indicators[fqn(pkgName, it.key)] = it.value
            indicators[it.key] = fqn(pkgName, it.key)
        }

        val quantities = HashMap<String, String>()
        pkgEnv.quantities.forEach {
            environment.quantities[fqn(pkgName, it.key)] = it.value
            quantities[it.key] = fqn(pkgName, it.key)
        }

        val units = HashMap<String, String>()
        pkgEnv.units.entries.forEach {
            environment.units[fqn(pkgName, it.key)] = it.value
            units[it.key] = fqn(pkgName, it.key)
        }

        val processTemplates = HashMap<String, String>()
        pkgEnv.processTemplates.forEach {
            environment.processTemplates[fqn(pkgName, it.key)] = it.value
            processTemplates[it.key] = fqn(pkgName, it.key)
        }

        return Substitution(
            products,
            substances,
            indicators,
            quantities,
            units,
            processTemplates,
            emptyMap(),
        )
    }
}

private data class Substitution(
    val products: Map<String, String>,
    val substances: Map<String, String>,
    val indicators: Map<String, String>,
    val quantities: Map<String, String>,
    val units: Map<String, String>,
    val processTemplates: Map<String, String>,
    val substanceCharacterizations: Map<String, String>,
) {

    companion object {
        fun empty(): Substitution {
            return Substitution(
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
    }

    fun plus(substitution: Substitution): Substitution {
        return Substitution(
            products.plus(substitution.products),
            substances.plus(substitution.substances),
            indicators.plus(substitution.indicators),
            quantities.plus(substitution.quantities),
            units.plus(substitution.units),
            processTemplates.plus(substitution.processTemplates),
            substanceCharacterizations.plus(substitution.substanceCharacterizations),
        )
    }

    fun apply(environment: Environment): Environment {
        val updateProductRef = everyProductRefInEnvironment.lift { ref ->
            products[ref.name]?.let { EProductRef(it) } ?: ref
        }
        val updateSubstanceRef = everySubstanceRefInEnvironment.lift { ref ->
            substances[ref.name]?.let { ESubstanceRef(it) } ?: ref
        }
        val updateIndicatorRef = everyIndicatorRefInEnvironment.lift { ref ->
            indicators[ref.name]?.let { EIndicatorRef(it) } ?: ref
        }
        val updateQuantityRef = everyQuantityRefInEnvironment.lift { ref ->
            quantities[ref.name]?.let { EQuantityRef(it) } ?: ref
        }
        val updateUnitRef = everyUnitRefInEnvironment.lift { ref ->
            units[ref.name]?.let { EUnitRef(it) } ?: ref
        }
        val updateTemplateRef = everyTemplateRefInEnvironment.lift { ref ->
            processTemplates[ref.name]?.let { ETemplateRef(it) } ?: ref
        }

        return listOf(
            updateProductRef,
            updateSubstanceRef,
            updateIndicatorRef,
            updateQuantityRef,
            updateUnitRef,
            updateTemplateRef,
        ).reduce { acc, function -> acc.compose(function) }
            .invoke(environment)
    }
}

private fun fqn(name: String, key: String): String {
    return "$name.$key"
}
