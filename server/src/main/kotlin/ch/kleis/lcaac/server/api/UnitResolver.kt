package ch.kleis.lcaac.server.api

import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.parseDataExpression

/**
 * Resolves the units of an API in a model: those of the prelude and those that the model declares.
 */
class UnitResolver(symbolTable: SymbolTable<BasicNumber>) {
    private val reducer = DataExpressionReducer(
        symbolTable.data, symbolTable.dataSources, BasicOperations, DummySourceOperations(),
    )

    /** The unit written [text], or null if it is not a known unit. */
    fun resolve(text: String): UnitValue<BasicNumber>? = try {
        val expression = CoreMapper(BasicOperations).dataExpression(parseDataExpression(text))
        // A unit reduces to the quantity 1 of it
        (with(ToValue(BasicOperations)) { reducer.reduce(expression).toValue() } as? QuantityValue<BasicNumber>)
            ?.takeIf { it.amount.value == 1.0 }
            ?.unit
    } catch (e: Exception) {
        null
    }

    /** The resolved units of [units], leaving out those that are unknown. */
    fun resolve(units: Units): ResolvedUnits = ResolvedUnits(
        units.impacts.mapNotNull { (k, v) -> resolve(v)?.let { k to it } }.toMap(),
        units.products.mapNotNull { (k, v) -> resolve(v)?.let { k to it } }.toMap(),
    )
}

data class ResolvedUnits(
    val impacts: Map<String, UnitValue<BasicNumber>>,
    val products: Map<String, UnitValue<BasicNumber>>,
)
