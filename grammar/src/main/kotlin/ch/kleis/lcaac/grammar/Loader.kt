package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.*
import ch.kleis.lcaac.core.lang.register.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.grammar.parser.LcaLangParser

enum class LoaderOption {
    WITH_PRELUDE
}

class Loader<Q>(
    ops: QuantityOperations<Q>,
) {
    private val mapper = CoreMapper(ops)

    fun load(
        files: Sequence<LcaLangParser.LcaFileContext>,
        options: List<LoaderOption> = emptyList(),
    ): SymbolTable<Q> {
        with(mapper) {
            val unitDefinitions = files.flatMap { it.unitDefinition() }
            val processDefinitions = files.flatMap { it.processDefinition() }
            val substanceDefinitions = files.flatMap { it.substanceDefinition() }
            val globalDefinitions = files.flatMap { it.globalVariables() }
                .flatMap { it.globalAssignment() }

            val dimensions = try {
                val register =
                    if (options.contains(LoaderOption.WITH_PRELUDE)) Prelude.dimensions()
                    else DimensionRegister.empty()
                register
                    .plus(
                        unitDefinitions
                            .filter { it.type() == UnitDefinitionType.LITERAL }
                            .map { DimensionKey(it.dimField().innerText()) to dimension(it.dimField()) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate reference units for dimensions ${e.duplicates}")
            }

            val substanceCharacterizations = try {
                SubstanceCharacterizationRegister<Q>()
                    .plus(
                        substanceDefinitions
                            .map { it.buildUniqueKey() to substanceCharacterization(it) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate substance ${e.duplicates} defined")
            }

            val globals = try {
                val register: DataRegister<Q> =
                    if (options.contains(LoaderOption.WITH_PRELUDE)) Prelude.units()
                    else DataRegister()
                register
                    .plus(
                        unitDefinitions
                            .filter { it.type() == UnitDefinitionType.LITERAL }
                            .map { DataKey(it.dataRef().innerText()) to unitLiteral(it) }
                            .asIterable()
                    )
                    .plus(
                        unitDefinitions
                            .filter { it.type() == UnitDefinitionType.ALIAS }
                            .map { DataKey(it.dataRef().innerText()) to unitAlias(it) }
                            .asIterable()
                    )
                    .plus(
                        globalDefinitions
                            .map { DataKey(it.dataRef().innerText()) to dataExpression(it.dataExpression()) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate global variable ${e.duplicates} defined")
            }


            val processTemplates = try {
                ProcessTemplateRegister<Q>()
                    .plus(
                        processDefinitions
                            .map { Pair(it.buildUniqueKey(), process(it, globals)) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate process ${e.duplicates} defined")
            }
            return SymbolTable(
                data = globals,
                processTemplates = processTemplates,
                dimensions = dimensions,
                substanceCharacterizations = substanceCharacterizations,
            )
        }
    }

}

