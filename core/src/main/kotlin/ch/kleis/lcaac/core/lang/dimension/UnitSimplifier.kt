package ch.kleis.lcaac.core.lang.dimension

import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.EUnitAlias
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.QuantityOperations
import kotlin.math.pow

/**
 * Simplifies quantities whose unit is a product of units, by removing the dimensionless factors:
 * `0.0825 kWh.u².percent⁻¹` is `8.25 kWh`, `15 kg.percent` is `0.15 kg`. The value does not change.
 */
class UnitSimplifier<Q>(
    private val ops: QuantityOperations<Q>,
    /** The known units, by symbol. */
    private val units: Map<String, UnitValue<Q>>,
) {
    companion object {
        /** A simplifier that knows the units of [symbolTable], from the prelude and the model. */
        fun <Q> of(symbolTable: SymbolTable<Q>, ops: QuantityOperations<Q>): UnitSimplifier<Q> {
            val reducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, DummySourceOperations())
            val units = symbolTable.globalVariables.toMap()
                .filter { it.value is EUnitLiteral || it.value is EUnitAlias }
                .mapNotNull { (key, _) ->
                    val reduced = reducer.reduce(EDataRef(key.name)) as? EQuantityScale<Q> ?: return@mapNotNull null
                    val literal = reduced.base as? EUnitLiteral<Q> ?: return@mapNotNull null
                    val scale = with(ops) { reduced.scale.toDouble() } * literal.scale
                    UnitValue<Q>(literal.symbol, scale, literal.dimension)
                }
                .associateBy { it.symbol.toString() }
            return UnitSimplifier(ops, units)
        }
    }

    /** The same quantity without the dimensionless factors of its unit, or null if there is nothing to simplify. */
    fun simplify(quantity: QuantityValue<Q>): QuantityValue<Q>? {
        val symbol = quantity.unit.symbol
        if (symbol.factor() != 1.0 || symbol.components().size < 2 && symbol.components().values.all { it == 1.0 }) return null
        val components = symbol.components().map { (name, exponent) -> (units[name] ?: return null) to exponent }
        val kept = components.filter { (unit, _) -> unit.dimension != Dimension.None }
        val simple = if (kept.isEmpty()) units["u"] ?: return null
        else kept.map { (unit, exponent) -> unit.pow(exponent) }.reduce { a, b -> a.times(b) }
        if (simple.dimension != quantity.unit.dimension || simple.symbol == symbol) return null
        val amount = with(ops) { quantity.amount * pure(quantity.unit.scale / simple.scale) }
        return QuantityValue(amount, simple)
    }

    private fun UnitValue<Q>.pow(exponent: Double) =
        UnitValue<Q>(symbol.pow(exponent), scale.pow(exponent), dimension.pow(exponent))

    private fun UnitValue<Q>.times(other: UnitValue<Q>) =
        UnitValue<Q>(symbol.multiply(other.symbol), scale * other.scale, dimension.multiply(other.dimension))
}
