package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.QuantityOperations
import kotlin.math.pow

class DataExpressionReducer<Q>(
    dataRegister: DataRegister<Q>,
    dataSourceRegister: DataSourceRegister<Q>,
    private val ops: QuantityOperations<Q>,
    private val sourceOps: DataSourceOperations<Q>,
) {
    private val dataRegister = DataRegister(dataRegister)
    private val dataSourceRegister = DataSourceRegister(dataSourceRegister)
    private val infiniteUnitLoopChecker = InfiniteUnitLoopChecker<Q>()

    fun reduce(expression: DataExpression<Q>): DataExpression<Q> {
        with(ops) {
            return when (expression) {
                is EDataRef -> reduceRef(expression)
                is EQuantityAdd -> reduceAdd(expression)
                is EQuantityClosure -> reduceClosure(expression)
                is EQuantityDiv -> reduceDiv(expression)
                is EQuantityMul -> reduceMul(expression)
                is EQuantityPow -> reducePow(expression)
                is EQuantityScale -> reduceScale(expression)
                is EQuantitySub -> reduceSub(expression)
                is EUnitAlias -> reduceAlias(expression)
                is EUnitLiteral -> EQuantityScale(pure(1.0), expression)
                is EUnitOf -> reduceUnitOf(expression)
                is EStringLiteral -> expression
                is ERecord -> reduceMap(expression)
                is ERecordEntry -> reduceMapEntry(expression)
                is EDefaultRecordOf -> reduceDefaultRecordOf(expression)
                is ESumProduct -> reduceESumProduct(expression)
                is EFirstRecordOf -> reduceFirstRecordOf(expression)
            }
        }
    }

    private fun reduceFirstRecordOf(expression: EFirstRecordOf<Q>): DataExpression<Q> {
        val dataSource = evalDataSource(expression.dataSource)
        return reduceMap(sourceOps.getFirst(dataSource))
    }

    fun reduceDataSource(expression: DataSourceExpression<Q>, filter: Map<String, DataExpression<Q>> = emptyMap()): EDataSource<Q> {
        return when (expression) {
            is EDataSource -> {
                val s = expression.schema
                    .mapValues { reduce(it.value) }
                val f = expression.filter.plus(filter)
                    .mapValues { reduce(it.value) }
                val invalidKeys = f.keys
                    .filter { s.containsKey(it) }
                    .filter { s[it]!! !is StringExpression }
                if (invalidKeys.isNotEmpty())
                    throw EvaluatorException("data source '${expression.config.name}': cannot match on numeric column(s) $invalidKeys")
                expression.copy(
                    schema = s,
                    filter = f,
                )
            }

            is EDataSourceRef -> dataSourceRegister[DataSourceKey(expression.name)]?.let { reduceDataSource(it, filter) }
                ?: throw EvaluatorException("unknown data source '${expression.name}'")

            is EFilter -> reduceDataSource(expression.dataSource, filter.plus(expression.filter))
        }
    }

    fun evalDataSource(expression: DataSourceExpression<Q>): DataSourceValue<Q> {
        return with(ToValue(ops)) {
            reduceDataSource(expression).toValue()
        }
    }

    private fun reduceESumProduct(expression: ESumProduct<Q>): DataExpression<Q> {
        val dataSource = evalDataSource(expression.dataSource)
        return reduce(sourceOps.sumProduct(dataSource, expression.columns))
    }

    private fun reduceDefaultRecordOf(expression: EDefaultRecordOf<Q>): DataExpression<Q> {
        val dataSource = reduceDataSource(expression.dataSource)
        val schema = dataSource.schema
        return ERecord(schema.mapValues { reduce(it.value) })
    }

    private fun reduceMapEntry(expression: ERecordEntry<Q>): DataExpression<Q> {
        return when (val map = reduce(expression.record)) {
            is ERecord -> map.entries[expression.index]
                ?: throw EvaluatorException("invalid index: '${expression.index}' not in ${map.entries.keys}")

            else -> ERecordEntry(map, expression.index)
        }
    }

    private fun reduceMap(expression: ERecord<Q>): DataExpression<Q> {
        return ERecord(expression.entries.mapValues {
            reduce(it.value)
        })
    }


    private fun reduceUnitOf(unitOf: EUnitOf<Q>): DataExpression<Q> {
        val reducedExpression = dummyReducer().reduce(unitOf.expression)
        return when {
            reducedExpression is EQuantityScale && reducedExpression.base is EUnitLiteral -> EQuantityScale(ops.pure(1.0), reducedExpression.base)

            reducedExpression is EUnitOf -> reducedExpression
            else -> EUnitOf(reducedExpression)
        }
    }

    private fun reduceAdd(expression: EQuantityAdd<Q>): DataExpression<Q> {
        with(ops) {
            val left = reduce(expression.leftHandSide)
            val right = reduce(expression.rightHandSide)
            return when {
                left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                    val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                    val resultAmount = (left.scale * pure(left.base.scale) + right.scale * pure(right.base.scale)) / pure(resultUnit.scale)
                    EQuantityScale(resultAmount, resultUnit)
                }

                else -> EQuantityAdd(left, right)
            }
        }
    }

    private fun reduceClosure(closure: EQuantityClosure<Q>): DataExpression<Q> = DataExpressionReducer(closure.symbolTable.data, closure.symbolTable.dataSources, ops, sourceOps).reduce(closure.expression)

    private fun reduceDiv(expression: EQuantityDiv<Q>): DataExpression<Q> {
        with(ops) {
            val left = reduce(expression.leftHandSide)
            val right = reduce(expression.rightHandSide)
            return when {
                left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral ->

                    EQuantityScale(left.scale / right.scale, EUnitLiteral(
                        left.base.symbol.divide(right.base.symbol),
                        left.base.scale / right.base.scale,
                        left.base.dimension.divide(right.base.dimension),
                    ))


                else -> EQuantityDiv(left, right)
            }
        }
    }

    private fun reduceMul(expression: EQuantityMul<Q>): DataExpression<Q> {
        with(ops) {
            val left = reduce(expression.leftHandSide)
            val right = reduce(expression.rightHandSide)
            return when {
                left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral ->

                    EQuantityScale(left.scale * right.scale, EUnitLiteral(
                        left.base.symbol.multiply(right.base.symbol),
                        left.base.scale * right.base.scale,
                        left.base.dimension.multiply(right.base.dimension),
                    ))

                else -> EQuantityMul(left, right)
            }
        }
    }

    private fun reducePow(expression: EQuantityPow<Q>): DataExpression<Q> {
        with(ops) {
            val quantity = reduce(expression.quantity)
            return when {
                quantity is EQuantityScale && quantity.base is EUnitLiteral -> EQuantityScale(
                    quantity.scale.pow(expression.exponent),
                    EUnitLiteral(
                        quantity.base.symbol.pow(expression.exponent),
                        quantity.base.scale.pow(expression.exponent),
                        quantity.base.dimension.pow(expression.exponent),
                    ),
                )

                else -> EQuantityPow(quantity, expression.exponent)
            }
        }
    }

    private fun reduceRef(expression: EDataRef<Q>): DataExpression<Q> {
        val key = DataKey(expression.name)
        return dataRegister[key]?.let { reduce(it) } ?: expression
    }

    private fun reduceScale(expression: EQuantityScale<Q>): DataExpression<Q> {
        with(ops) {
            return when (val base = reduce(expression.base)) {
                is EQuantityScale -> EQuantityScale(expression.scale * base.scale, base.base)
                else -> EQuantityScale(expression.scale, base)
            }
        }
    }

    private fun reduceSub(expression: EQuantitySub<Q>): DataExpression<Q> {
        with(ops) {
            val left = reduce(expression.leftHandSide)
            val right = reduce(expression.rightHandSide)
            return when {
                left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                    val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                    val resultAmount = (left.scale * pure(left.base.scale) - right.scale * pure(right.base.scale)) / pure(resultUnit.scale)
                    EQuantityScale(resultAmount, resultUnit)
                }

                else -> EQuantitySub(left, right)
            }
        }
    }

    private fun reduceAlias(expression: EUnitAlias<Q>): DataExpression<Q> {
        with(ops) {
            val aliasForExpression = reduceAliasFor(expression)
            return when {
                aliasForExpression is EQuantityScale && aliasForExpression.base is EUnitLiteral -> {
                    EQuantityScale(pure(1.0), EUnitLiteral(UnitSymbol.of(expression.symbol), aliasForExpression.scale.toDouble() * aliasForExpression.base.scale, aliasForExpression.base.dimension))
                }

                else -> EUnitAlias(expression.symbol, aliasForExpression)
            }
        }
    }

    private fun reduceAliasFor(expression: EUnitAlias<Q>): DataExpression<Q> {
        infiniteUnitLoopChecker.check(expression)
        val aliasForExpression = reduce(expression.aliasFor)
        infiniteUnitLoopChecker.clearTraceAlias()
        return aliasForExpression
    }

    private fun checkAndReturnUnitForAddition(left: EUnitLiteral<Q>, right: EUnitLiteral<Q>): EUnitLiteral<Q> {
        if (left.dimension != right.dimension) {
            throw EvaluatorException("incompatible dimensions: ${left.dimension} vs ${right.dimension} in left=$left and right=$right")
        }

        return if (left.scale > right.scale) left else right
    }


    private fun dummyReducer(): DataExpressionReducer<Q> {
        return DataExpressionReducer(dataRegister, dataSourceRegister, ops, DummySourceOperations())
    }
}
