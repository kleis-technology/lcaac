package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.RegisterException
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.grammar.parser.LcaLangParser

class Loader<Q>(
    ops: QuantityOperations<Q>,
) {
    private val mapper = CoreMapper(ops)

    fun load(files: Sequence<LcaLangParser.LcaFileContext>): SymbolTable<Q> {
        with(mapper) {
            val unitDefinitions = files.flatMap { it.unitDefinition() }
            val processDefinitions = files.flatMap { it.processDefinition() }
            val substanceDefinitions = files.flatMap { it.substanceDefinition() }
            val globalDefinitions = files.flatMap { it.globalVariables() }
                .flatMap { it.globalAssignment() }

            val dimensions = try {
                Register.empty<Dimension>()
                    .plus(
                        unitDefinitions
                            .filter { it.type() == CoreMapper.UnitDefinitionType.LITERAL }
                            .map { it.dimField().innerText() to dimension(it.dimField()) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate reference units for dimensions ${e.duplicates}")
            }

            val substanceCharacterizations = try {
                Register.empty<ESubstanceCharacterization<Q>>()
                    .plus(
                        substanceDefinitions
                            .map { it.substanceRef().innerText() to substanceCharacterization(it) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate substance ${e.duplicates} defined")
            }

            val globals = try {
                Register.empty<DataExpression<Q>>()
                    .plus(
                        unitDefinitions
                            .filter { it.type() == CoreMapper.UnitDefinitionType.LITERAL }
                            .map { it.dataRef().innerText() to unitLiteral(it) }
                            .asIterable()
                    )
                    .plus(
                        unitDefinitions
                            .filter { it.type() == CoreMapper.UnitDefinitionType.ALIAS }
                            .map { it.dataRef().innerText() to unitAlias(it) }
                            .asIterable()
                    )
                    .plus(
                        globalDefinitions
                            .map { it.dataRef().innerText() to dataExpression(it.dataExpression()) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw LoaderException("Duplicate global variable ${e.duplicates} defined")
            }


            val processTemplates = try {
                Register.empty<EProcessTemplate<Q>>()
                    .plus(
                        processDefinitions
                            .map { Pair(it.name.innerText(), process(it, globals)) }
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
