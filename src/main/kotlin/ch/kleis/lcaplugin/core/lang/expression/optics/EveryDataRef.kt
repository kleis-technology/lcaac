package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

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
            }
        }
    }

private fun <Q> everyDataRefInConstraint(): PEvery<FromProcess<Q>, FromProcess<Q>, EDataRef<Q>, DataExpression<Q>> =
    FromProcess.arguments<Q>() compose
        Every.map() compose everyDataRefInDataExpression<Q>()

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
    EBioExchange.quantity<Q>() compose everyDataRefInDataExpression<Q>()

private fun <Q> everyDataRefInEImpact(): PEvery<EImpact<Q>, EImpact<Q>, EDataRef<Q>, DataExpression<Q>> =
    EImpact.quantity<Q>() compose everyDataRefInDataExpression<Q>()

fun <Q> everyDataRefInProcess(): PEvery<EProcess<Q>, EProcess<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            EProcess.products<Q>() compose Every.list() compose everyDataRefInETechnoExchange(),
            EProcess.inputs<Q>() compose Every.list() compose everyDataRefInETechnoExchange(),
            EProcess.biosphere<Q>() compose Every.list() compose everyDataRefInEBioExchange(),
            EProcess.impacts<Q>() compose Every.list() compose everyDataRefInEImpact(),
        )
    )

private fun <Q> everyDataRefInSubstanceCharacterization(): PEvery<ESubstanceCharacterization<Q>, ESubstanceCharacterization<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange<Q>() compose everyDataRefInEBioExchange(),
            ESubstanceCharacterization.impacts<Q>() compose Every.list() compose everyDataRefInEImpact(),
        )
    )


private fun <Q> everyDataRefInLcaExpression(): PEvery<LcaExpression<Q>, LcaExpression<Q>, EDataRef<Q>, DataExpression<Q>> =
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

private fun <Q> everyDataRefInTemplateExpression(): PEvery<ProcessTemplateExpression<Q>, ProcessTemplateExpression<Q>, EDataRef<Q>, DataExpression<Q>> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression<Q>() compose Merge(
                listOf(
                    EProcessTemplate.params<Q>() compose Every.map() compose
                        everyDataRefInDataExpression(),
                    EProcessTemplate.locals<Q>() compose Every.map() compose
                        everyDataRefInDataExpression(),
                    EProcessTemplate.body<Q>() compose everyDataRefInProcess(),
                )
            ),
            ProcessTemplateExpression.eProcessFinal<Q>().expression() compose everyDataRefInProcess(),
        ),
    )

fun <Q> everyDataRef(): PEvery<Expression<Q>, Expression<Q>, EDataRef<Q>, EDataRef<Q>> =
    Merge(
        listOf(
            Expression.lcaExpression<Q>() compose everyDataRefInLcaExpression(),
            Expression.dataExpression<Q>() compose everyDataRefInDataExpression(),
            Expression.processTemplateExpression<Q>() compose everyDataRefInTemplateExpression(),
        )
    )
