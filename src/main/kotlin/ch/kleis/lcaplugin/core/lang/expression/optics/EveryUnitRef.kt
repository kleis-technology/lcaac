package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*


val everyUnitRefInUnitExpression = object : PEvery<UnitExpression, UnitExpression, EUnitRef, UnitExpression> {
    override fun <R> foldMap(M: Monoid<R>, source: UnitExpression, map: (focus: EUnitRef) -> R): R {
        return when (source) {
            is EUnitMul -> M.fold(
                listOf(
                    foldMap(M, source.left, map),
                    foldMap(M, source.right, map),
                )
            )

            is EUnitDiv -> M.fold(
                listOf(
                    foldMap(M, source.left, map),
                    foldMap(M, source.right, map),
                )
            )

            is EUnitPow -> foldMap(M, source.unit, map)
            is EUnitLiteral -> M.empty()
            is EUnitRef -> map(source)
            is EUnitOf -> everyUnitRefInQuantityExpression.foldMap(M, source.quantity, map)
            is EUnitClosure -> foldMap(M, source.expression, map)
            is EUnitAlias -> everyUnitRefInQuantityExpression.foldMap(M, source.aliasFor, map)
        }
    }

    override fun modify(source: UnitExpression, map: (focus: EUnitRef) -> UnitExpression): UnitExpression {
        return when (source) {
            is EUnitDiv -> EUnitDiv(
                modify(source.left, map),
                modify(source.right, map),
            )

            is EUnitLiteral -> source
            is EUnitMul -> EUnitMul(
                modify(source.left, map),
                modify(source.right, map),
            )

            is EUnitPow -> EUnitPow(
                modify(source.unit, map),
                source.exponent,
            )

            is EUnitRef -> map(source)
            is EUnitOf -> EUnitOf(
                everyUnitRefInQuantityExpression.modify(source.quantity, map)
            )

            is EUnitClosure -> EUnitClosure(
                source.symbolTable,
                modify(source.expression, map),
            )
            is EUnitAlias -> EUnitAlias(
                source.symbol,
                everyUnitRefInQuantityExpression.modify(source.aliasFor, map)
            )
        }
    }
}

val everyUnitRefInQuantityExpression: PEvery<QuantityExpression, QuantityExpression, EUnitRef, UnitExpression> =
    everyQuantityLiteralInQuantityExpression compose
            EQuantityLiteral.unit compose
            everyUnitRefInUnitExpression

val everyUnitRefInProduct: PEvery<EProduct, EProduct, EUnitRef, UnitExpression> =
    EProduct.referenceUnit compose everyUnitRefInUnitExpression

val everyUnitRefInUnconstrainedProductExpression: PEvery<LcaUnconstrainedProductExpression, LcaUnconstrainedProductExpression, EUnitRef, UnitExpression> =
    LcaUnconstrainedProductExpression.eProduct compose everyUnitRefInProduct

val everyUnitRefInConstraint: PEvery<Constraint, Constraint, EUnitRef, UnitExpression> =
    Constraint.fromProcessRef.arguments compose
            Every.map() compose
            everyUnitRefInQuantityExpression

val everyUnitRefInConstrainedProduct =
    Merge(
        listOf(
            EConstrainedProduct.product compose everyUnitRefInUnconstrainedProductExpression,
            EConstrainedProduct.constraint compose everyUnitRefInConstraint,
        )
    )

val everyUnitRefInETechnoExchange: PEvery<ETechnoExchange, ETechnoExchange, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            ETechnoExchange.quantity compose everyUnitRefInQuantityExpression,
            ETechnoExchange.product compose everyUnitRefInConstrainedProduct,
        )
    )


val everyUnitRefInSubstanceExpression: PEvery<LcaSubstanceExpression, LcaSubstanceExpression, EUnitRef, UnitExpression> =
    LcaSubstanceExpression.eSubstance.referenceUnit compose everyUnitRefInUnitExpression

val everyUnitRefInEBioExchange: PEvery<EBioExchange, EBioExchange, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            EBioExchange.quantity compose everyUnitRefInQuantityExpression,
            EBioExchange.substance compose everyUnitRefInSubstanceExpression,
        )
    )

val everyUnitRefInIndicatorExpression: PEvery<LcaIndicatorExpression, LcaIndicatorExpression, EUnitRef, UnitExpression> =
    LcaIndicatorExpression.eIndicator.referenceUnit compose everyUnitRefInUnitExpression

val everyUnitRefInEImpact: PEvery<EImpact, EImpact, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            EImpact.quantity compose everyUnitRefInQuantityExpression,
            EImpact.indicator compose everyUnitRefInIndicatorExpression,
        )
    )


val everyUnitRefInProcess: PEvery<EProcess, EProcess, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose everyUnitRefInETechnoExchange,
            EProcess.inputs compose Every.list() compose everyUnitRefInETechnoExchange,
            EProcess.biosphere compose Every.list() compose everyUnitRefInEBioExchange,
        )
    )


val everyUnitRefInSubstanceCharacterization: PEvery<ESubstanceCharacterization, ESubstanceCharacterization, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange compose everyUnitRefInEBioExchange,
            ESubstanceCharacterization.impacts compose Every.list() compose everyUnitRefInEImpact,
        )
    )


val everyUnitRefInLcaExpression: PEvery<LcaExpression, LcaExpression, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            LcaExpression.eProcess compose everyUnitRefInProcess,
            LcaExpression.lcaExchangeExpression.eTechnoExchange compose everyUnitRefInETechnoExchange,
            LcaExpression.lcaExchangeExpression.eBioExchange compose everyUnitRefInEBioExchange,
            LcaExpression.lcaExchangeExpression.eImpact compose everyUnitRefInEImpact,
            LcaExpression.eConstrainedProduct compose everyUnitRefInConstrainedProduct,
            LcaExpression.lcaIndicatorExpression compose everyUnitRefInIndicatorExpression,
            LcaExpression.lcaSubstanceExpression compose everyUnitRefInSubstanceExpression,
            LcaExpression.eSubstanceCharacterization compose everyUnitRefInSubstanceCharacterization,
        )
    )

val everyUnitRefInTemplateExpression: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression compose Merge(
                listOf(
                    EProcessTemplate.params compose Every.map() compose everyUnitRefInQuantityExpression,
                    EProcessTemplate.locals compose Every.map() compose everyUnitRefInQuantityExpression,
                    EProcessTemplate.body compose everyUnitRefInProcess,
                )
            ),
            ProcessTemplateExpression.eProcessFinal.expression compose everyUnitRefInProcess,
        )
    )

val everyUnitRef: Every<Expression, EUnitRef> =
    Merge(
        listOf(
            Expression.unitExpression compose everyUnitRefInUnitExpression,
            Expression.quantityExpression compose everyUnitRefInQuantityExpression,
            Expression.lcaExpression compose everyUnitRefInLcaExpression,
            Expression.processTemplateExpression compose everyUnitRefInTemplateExpression,
            Expression.systemExpression.eSystem.processes compose Every.list() compose everyUnitRefInProcess,
        )
    )
