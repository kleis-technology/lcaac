package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.Fold
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

val everyDataRefInDataExpression =
    object : PEvery<DataExpression, DataExpression, EDataRef, DataExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: DataExpression, map: (focus: EDataRef) -> R): R {
            return when (source) {
                is EDataRef -> map(source)
                is EQuantityAdd -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantitySub -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityMul -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityDiv -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
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

        override fun modify(source: DataExpression, map: (focus: EDataRef) -> DataExpression): DataExpression {
            return when (source) {
                is EDataRef -> map(source)
                is EQuantityAdd -> EQuantityAdd(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantitySub -> EQuantitySub(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityMul -> EQuantityMul(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityDiv -> EQuantityDiv(
                    modify(source.left, map),
                    modify(source.right, map),
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

private val everyDataRefInConstraint: PEvery<FromProcess, FromProcess, EDataRef, DataExpression> =
    FromProcess.arguments compose
        Every.map() compose
        everyDataRefInDataExpression

private val everyDataRefInEProductSpec: PEvery<EProductSpec, EProductSpec, EDataRef, DataExpression> =
    Merge(
        listOf(
            EProductSpec.fromProcess compose everyDataRefInConstraint,
            EProductSpec.fromProcess.arguments compose
                Every.map() compose
                everyDataRefInDataExpression,
            EProductSpec.fromProcess.matchLabels.elements compose
                Every.map() compose
                everyDataRefInDataExpression,
        )
    )

private val everyDataRefInETechnoExchange: PEvery<ETechnoExchange, ETechnoExchange, EDataRef, DataExpression> =
    Merge(
        listOf(
            ETechnoExchange.quantity compose everyDataRefInDataExpression,
            ETechnoExchange.product compose everyDataRefInEProductSpec,
        )
    )

private val everyDataRefInEBioExchange: PEvery<EBioExchange, EBioExchange, EDataRef, DataExpression> =
    EBioExchange.quantity compose everyDataRefInDataExpression

private val everyDataRefInEImpact: PEvery<EImpact, EImpact, EDataRef, DataExpression> =
    EImpact.quantity compose everyDataRefInDataExpression

val everyDataRefInProcess: PEvery<EProcess, EProcess, EDataRef, DataExpression> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose everyDataRefInETechnoExchange,
            EProcess.inputs compose Every.list() compose everyDataRefInETechnoExchange,
            EProcess.biosphere compose Every.list() compose everyDataRefInEBioExchange,
        )
    )

private val everyDataRefInSubstanceCharacterization: PEvery<ESubstanceCharacterization, ESubstanceCharacterization, EDataRef, out DataExpression> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange compose everyDataRefInEBioExchange,
            ESubstanceCharacterization.impacts compose Every.list() compose everyDataRefInEImpact,
        )
    )


private val everyDataRefInSystemExpression: PEvery<SystemExpression, SystemExpression, EDataRef, out DataExpression> =
    Merge(
        listOf(
            SystemExpression.eSystem.processes compose
                Every.list() compose
                everyDataRefInProcess,
            SystemExpression.eSystem.substanceCharacterizations compose
                Every.list() compose
                everyDataRefInSubstanceCharacterization,
        )
    )

private val everyDataRefInLcaExpression: PEvery<LcaExpression, LcaExpression, EDataRef, out DataExpression> =
    Merge(
        listOf(
            LcaExpression.eProcess compose
                everyDataRefInProcess,
            LcaExpression.lcaExchangeExpression.eTechnoExchange compose
                everyDataRefInETechnoExchange,
            LcaExpression.lcaExchangeExpression.eBioExchange compose
                everyDataRefInEBioExchange,
            LcaExpression.eProductSpec.fromProcess.arguments compose
                Every.map() compose
                everyDataRefInDataExpression
        )
    )

private val everyDataRefInTemplateExpression: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EDataRef, out DataExpression> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression compose Merge(
                listOf(
                    EProcessTemplate.params compose Every.map() compose
                        everyDataRefInDataExpression,
                    EProcessTemplate.locals compose Every.map() compose
                        everyDataRefInDataExpression,
                    EProcessTemplate.body compose everyDataRefInProcess,
                )
            ),
            ProcessTemplateExpression.eProcessFinal.expression compose everyDataRefInProcess,
        ),
    )

val everyDataRef: Fold<Expression, EDataRef> =
    Merge(
        listOf(
            Expression.dataExpression compose everyDataRefInDataExpression,
            Expression.lcaExpression compose everyDataRefInLcaExpression,
            Expression.processTemplateExpression compose everyDataRefInTemplateExpression,
            Expression.systemExpression compose everyDataRefInSystemExpression,
        )
    )
