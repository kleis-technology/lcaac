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
            is EUnitAlias -> TODO()
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
            is EUnitAlias -> TODO()
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

val everyUnitRefInEConstrainedProduct =
    Merge(
        listOf(
            EConstrainedProduct.product compose everyUnitRefInUnconstrainedProductExpression,
            EConstrainedProduct.constraint compose everyUnitRefInConstraint,
        )
    )

val everyUnitRefInProductExpression: PEvery<LcaProductExpression, LcaProductExpression, EUnitRef, UnitExpression> =
    LcaProductExpression.eConstrainedProduct compose
            everyUnitRefInEConstrainedProduct

val everyUnitRefInETechnoExchange: PEvery<ETechnoExchange, ETechnoExchange, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            ETechnoExchange.quantity compose everyUnitRefInQuantityExpression,
            ETechnoExchange.product compose everyUnitRefInProductExpression,
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

val everyUnitRefInProcessExpression: PEvery<LcaProcessExpression, LcaProcessExpression, EUnitRef, UnitExpression> =
    LcaProcessExpression.eProcess compose everyUnitRefInProcess


val everyUnitRefInSubstanceCharacterization: PEvery<ESubstanceCharacterization, ESubstanceCharacterization, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange compose everyUnitRefInEBioExchange,
            ESubstanceCharacterization.impacts compose Every.list() compose everyUnitRefInEImpact,
        )
    )

val everyUnitRefInSubstanceCharacterizationExpression: PEvery<LcaSubstanceCharacterizationExpression, LcaSubstanceCharacterizationExpression, EUnitRef, UnitExpression> =
    LcaSubstanceCharacterizationExpression.eSubstanceCharacterization compose everyUnitRefInSubstanceCharacterization

val everyUnitRefInSystemExpression: PEvery<SystemExpression, SystemExpression, EUnitRef, UnitExpression> =
    SystemExpression.eSystem.processes compose
            Every.list() compose
            everyUnitRefInProcessExpression

val everyUnitRefInLcaExpression: PEvery<LcaExpression, LcaExpression, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            LcaExpression.lcaProcessExpression compose everyUnitRefInProcessExpression,
            LcaExpression.lcaExchangeExpression.eTechnoExchange compose everyUnitRefInETechnoExchange,
            LcaExpression.lcaExchangeExpression.eBioExchange compose everyUnitRefInEBioExchange,
            LcaExpression.lcaExchangeExpression.eImpact compose everyUnitRefInEImpact,
            LcaExpression.lcaProductExpression compose everyUnitRefInProductExpression,
            LcaExpression.lcaIndicatorExpression compose everyUnitRefInIndicatorExpression,
            LcaExpression.lcaSubstanceExpression compose everyUnitRefInSubstanceExpression,
            LcaExpression.lcaSubstanceCharacterizationExpression compose everyUnitRefInSubstanceCharacterizationExpression,
        )
    )

val everyUnitRefInTemplateExpression: PEvery<TemplateExpression, TemplateExpression, EUnitRef, UnitExpression> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression compose Merge(
                listOf(
                    EProcessTemplate.params compose Every.map() compose everyUnitRefInQuantityExpression,
                    EProcessTemplate.locals compose Every.map() compose everyUnitRefInQuantityExpression,
                    EProcessTemplate.body compose everyUnitRefInProcessExpression,
                )
            ),
            TemplateExpression.eProcessFinal.expression compose everyUnitRefInProcessExpression,
        )
    )

val everyUnitRef: Every<Expression, EUnitRef> =
    Merge(
        listOf(
            Expression.unitExpression compose everyUnitRefInUnitExpression,
            Expression.quantityExpression compose everyUnitRefInQuantityExpression,
            Expression.lcaExpression compose everyUnitRefInLcaExpression,
            Expression.templateExpression compose everyUnitRefInTemplateExpression,
            Expression.systemExpression.eSystem.processes compose Every.list() compose everyUnitRefInProcessExpression,
        )
    )
