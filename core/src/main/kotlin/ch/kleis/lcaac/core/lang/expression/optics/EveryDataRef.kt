@file:Suppress("LocalVariableName")

package ch.kleis.lcaac.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.*

fun <Q> everyDataRefInDataExpression(): PEvery<DataExpression<Q>, DataExpression<Q>, EDataRef<Q>, DataExpression<Q>> =
    object : PEvery<DataExpression<Q>, DataExpression<Q>, EDataRef<Q>, DataExpression<Q>> {
        override fun <R> foldMap(M: Monoid<R>, source: DataExpression<Q>, map: (focus: EDataRef<Q>) -> R): R {
            return when (source) {
                is EDataRef -> map(source)
                is EQuantityAdd -> M.fold(
                    listOf(
                        foldMap(M, source.leftHandSide, map),
                        foldMap(M, source.rightHandSide, map),
                    )
                )

                is EQuantitySub -> M.fold(
                    listOf(
                        foldMap(M, source.leftHandSide, map),
                        foldMap(M, source.rightHandSide, map),
                    )
                )

                is EQuantityMul -> M.fold(
                    listOf(
                        foldMap(M, source.leftHandSide, map),
                        foldMap(M, source.rightHandSide, map),
                    )
                )

                is EQuantityDiv -> M.fold(
                    listOf(
                        foldMap(M, source.leftHandSide, map),
                        foldMap(M, source.rightHandSide, map),
                    )
                )

                is EQuantityClosure -> foldMap(M, source.expression, map)
                is EQuantityPow -> foldMap(M, source.quantity, map)
                is EQuantityScale -> foldMap(M, source.base, map)
                is EUnitAlias -> foldMap(M, source.aliasFor, map)
                is EUnitLiteral -> M.empty()
                is EUnitOf -> foldMap(M, source.expression, map)
                is EStringLiteral -> M.empty()
                is ERecord -> M.fold(
                    source.entries.values
                        .map { foldMap(M, it, map) }
                )

                is ERecordEntry -> foldMap(M, source.record, map)
                is EDefaultRecordOf -> M.fold(
                    (everyDataExpressionInDataSourceExpression<Q>() compose
                        DataExpression.eDataRef()).getAll(source.dataSource)
                        .map(map)
                )

                is ESumProduct -> M.fold(
                    (everyDataExpressionInDataSourceExpression<Q>() compose
                        DataExpression.eDataRef()).getAll(source.dataSource)
                        .map(map)
                )

                is EFirstRecordOf -> M.fold(
                    (everyDataExpressionInDataSourceExpression<Q>() compose
                        DataExpression.eDataRef()).getAll(source.dataSource)
                        .map(map)
                )
            }
        }

        override fun modify(
            source: DataExpression<Q>,
            map: (focus: EDataRef<Q>) -> DataExpression<Q>
        ): DataExpression<Q> {
            return when (source) {
                is EDataRef -> map(source)
                is EQuantityAdd -> EQuantityAdd(
                    modify(source.leftHandSide, map),
                    modify(source.rightHandSide, map),
                )

                is EQuantitySub -> EQuantitySub(
                    modify(source.leftHandSide, map),
                    modify(source.rightHandSide, map),
                )

                is EQuantityMul -> EQuantityMul(
                    modify(source.leftHandSide, map),
                    modify(source.rightHandSide, map),
                )

                is EQuantityDiv -> EQuantityDiv(
                    modify(source.leftHandSide, map),
                    modify(source.rightHandSide, map),
                )

                is EQuantityClosure -> EQuantityClosure(
                    source.symbolTable,
                    modify(source.expression, map),
                )

                is EQuantityPow -> EQuantityPow(
                    modify(source.quantity, map),
                    source.exponent,
                )

                is EQuantityScale -> EQuantityScale(
                    source.scale,
                    modify(source.base, map),
                )

                is EUnitAlias -> EUnitAlias(
                    source.symbol,
                    modify(source.aliasFor, map),
                )

                is EUnitLiteral -> source
                is EUnitOf -> EUnitOf(
                    modify(source.expression, map),
                )

                is EStringLiteral -> source
                is ERecord -> ERecord(
                    source.entries.mapValues {
                        modify(it.value, map)
                    }
                )

                is ERecordEntry -> ERecordEntry(
                    modify(source.record, map),
                    source.index,
                )

                is EDefaultRecordOf -> source.copy(
                    dataSource = everyDataExpressionInDataSourceExpression<Q>().modify(source.dataSource) {
                        modify(it, map)
                    }
                )

                is ESumProduct -> source.copy(
                    dataSource = everyDataExpressionInDataSourceExpression<Q>().modify(source.dataSource) {
                        modify(it, map)
                    }
                )

                is EFirstRecordOf -> source.copy(
                    dataSource = everyDataExpressionInDataSourceExpression<Q>().modify(source.dataSource) {
                        modify(it, map)
                    }
                )
            }
        }
    }

private fun <Q> everyDataRefInConstraint(): PEvery<FromProcess<Q>, FromProcess<Q>, EDataRef<Q>, DataExpression<Q>> =
    FromProcess.arguments<Q>() compose
        Every.map() compose everyDataRefInDataExpression()

private fun <Q> everyDataRefInEProductSpec(): PEvery<EProductSpec<Q>, EProductSpec<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            EProductSpec.fromProcess<Q>() compose everyDataRefInConstraint(),
            EProductSpec.fromProcess<Q>().arguments() compose
                Every.map() compose
                everyDataRefInDataExpression(),
            EProductSpec.fromProcess<Q>().matchLabels().elements() compose
                Every.map() compose
                everyDataRefInDataExpression(),
        )
    )

private fun <Q> everyDataRefInETechnoExchange(): PEvery<ETechnoExchange<Q>, ETechnoExchange<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            ETechnoExchange.quantity<Q>() compose everyDataRefInDataExpression(),
            ETechnoExchange.product<Q>() compose everyDataRefInEProductSpec(),
        )
    )

private fun <Q> everyDataRefInEBioExchange(): PEvery<EBioExchange<Q>, EBioExchange<Q>, EDataRef<Q>, DataExpression<Q>> =
    EBioExchange.quantity<Q>() compose everyDataRefInDataExpression()

private fun <Q> everyDataRefInEImpact(): PEvery<EImpact<Q>, EImpact<Q>, EDataRef<Q>, DataExpression<Q>> =
    EImpact.quantity<Q>() compose everyDataRefInDataExpression()

private fun <E, Q> everyProperDataRefInEBlockForEach(): PEvery<EBlockForEach<E, Q>, EBlockForEach<E, Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            EBlockForEach.dataSource<E, Q>() compose
                everyDataExpressionInDataSourceExpression() compose
                everyDataRefInDataExpression(),
            EBlockForEach.locals<E, Q>() compose
                Every.map() compose
                everyDataRefInDataExpression(),
        )
    )

fun <Q> everyDataRefInProcess(): PEvery<EProcess<Q>, EProcess<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            EProcess.products<Q>() compose Every.list() compose everyDataRefInETechnoExchange(),
            EProcess.inputs<Q>() compose Every.list() compose
                BlockExpression.everyDataRef(everyDataRefInETechnoExchange()),
            EProcess.biosphere<Q>() compose Every.list() compose
                BlockExpression.everyDataRef(everyDataRefInEBioExchange()),
            EProcess.impacts<Q>() compose Every.list() compose
                BlockExpression.everyDataRef(everyDataRefInEImpact()),
        )
    )

private fun <Q> everyDataRefInSubstanceCharacterization(): PEvery<ESubstanceCharacterization<Q>, ESubstanceCharacterization<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange<Q>() compose everyDataRefInEBioExchange(),
            ESubstanceCharacterization.impacts<Q>() compose Every.list() compose
                BlockExpression.everyDataRef(everyDataRefInEImpact()),
        )
    )


fun <Q> everyDataRefInLcaExpression(): PEvery<LcaExpression<Q>, LcaExpression<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            LcaExpression.eProcess<Q>() compose
                everyDataRefInProcess(),
            LcaExpression.lcaExchangeExpression<Q>().eTechnoExchange() compose
                everyDataRefInETechnoExchange(),
            LcaExpression.lcaExchangeExpression<Q>().eBioExchange() compose
                everyDataRefInEBioExchange(),
            LcaExpression.eProductSpec<Q>().fromProcess().arguments() compose
                Every.map() compose
                everyDataRefInDataExpression(),
            LcaExpression.eSubstanceCharacterization<Q>() compose
                everyDataRefInSubstanceCharacterization()
        )
    )
