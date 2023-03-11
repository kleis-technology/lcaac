package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.Environment
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.processTemplates
import ch.kleis.lcaplugin.core.lang.quantities

val everyQuantityRefInQuantityExpression =
    object : PEvery<QuantityExpression, QuantityExpression, EQuantityRef, QuantityExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: QuantityExpression, map: (focus: EQuantityRef) -> R): R {
            return when (source) {
                is EQuantityAdd -> M.fold(
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

                is EQuantityLiteral -> M.empty()
                is EQuantityMul -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityNeg -> foldMap(M, source.quantity, map)
                is EQuantityPow -> foldMap(M, source.quantity, map)
                is EQuantityRef -> map(source)
                is EQuantitySub -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )
            }
        }

        override fun modify(
            source: QuantityExpression,
            map: (focus: EQuantityRef) -> QuantityExpression
        ): QuantityExpression {
            return when (source) {
                is EQuantityAdd -> EQuantityAdd(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityDiv -> EQuantityDiv(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityLiteral -> source
                is EQuantityMul -> EQuantityMul(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityNeg -> EQuantityNeg(
                    modify(source.quantity, map),
                )

                is EQuantityPow -> EQuantityPow(
                    modify(source.quantity, map),
                    source.exponent,
                )

                is EQuantityRef -> map(source)
                is EQuantitySub -> EQuantitySub(
                    modify(source.left, map),
                    modify(source.right, map),
                )
            }
        }
    }

private val everyQuantityRefInConstraint: PEvery<Constraint, Constraint, EQuantityRef, QuantityExpression> =
    Constraint.fromProcessRef.arguments compose
            Every.map() compose
            everyQuantityRefInQuantityExpression

private val everyQuantityRefInProductExpression: PEvery<LcaProductExpression, LcaProductExpression, EQuantityRef, QuantityExpression> =
    LcaProductExpression.eConstrainedProduct.constraint compose everyQuantityRefInConstraint

private val everyQuantityRefInETechnoExchange: PEvery<ETechnoExchange, ETechnoExchange, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            ETechnoExchange.quantity compose everyQuantityRefInQuantityExpression,
            ETechnoExchange.product compose everyQuantityRefInProductExpression,
        )
    )

private val everyQuantityRefInEBioExchange: PEvery<EBioExchange, EBioExchange, EQuantityRef, QuantityExpression> =
    EBioExchange.quantity compose everyQuantityRefInQuantityExpression

private val everyQuantityRefInEImpact: PEvery<EImpact, EImpact, EQuantityRef, QuantityExpression> =
    EImpact.quantity compose everyQuantityRefInQuantityExpression

private val everyQuantityRefInProcess: PEvery<EProcess, EProcess, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose everyQuantityRefInETechnoExchange,
            EProcess.inputs compose Every.list() compose everyQuantityRefInETechnoExchange,
            EProcess.biosphere compose Every.list() compose everyQuantityRefInEBioExchange,
        )
    )

val everyQuantityRefInProcessExpression: PEvery<LcaProcessExpression, LcaProcessExpression, EQuantityRef, QuantityExpression> =
    LcaProcessExpression.eProcess compose everyQuantityRefInProcess

private val everyQuantityRefInSubstanceCharacterization: PEvery<ESubstanceCharacterization, ESubstanceCharacterization, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange compose everyQuantityRefInEBioExchange,
            ESubstanceCharacterization.impacts compose Every.list() compose everyQuantityRefInEImpact,
        )
    )

private val everyQuantityRefInSubstanceCharacterizationExpression: PEvery<LcaSubstanceCharacterizationExpression, LcaSubstanceCharacterizationExpression, EQuantityRef, QuantityExpression> =
    LcaSubstanceCharacterizationExpression.eSubstanceCharacterization compose
            everyQuantityRefInSubstanceCharacterization

private val everyQuantityRefInSystemExpression: PEvery<SystemExpression, SystemExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            SystemExpression.eSystem.processes compose
                    Every.list() compose
                    everyQuantityRefInProcessExpression,
            SystemExpression.eSystem.substanceCharacterizations compose
                    Every.list() compose
                    everyQuantityRefInSubstanceCharacterizationExpression,
        )
    )

private val everyQuantityRefInLcaExpression: PEvery<LcaExpression, LcaExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            LcaExpression.lcaProcessExpression compose
                    everyQuantityRefInProcessExpression,
            LcaExpression.lcaExchangeExpression.eTechnoExchange compose
                    everyQuantityRefInETechnoExchange,
            LcaExpression.lcaExchangeExpression.eBioExchange compose
                    everyQuantityRefInEBioExchange,
            LcaExpression.lcaProductExpression.eConstrainedProduct.constraint.fromProcessRef.arguments compose
                    Every.map() compose everyQuantityRefInQuantityExpression
        )
    )

private val everyQuantityRefInTemplateExpression: PEvery<TemplateExpression, TemplateExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression compose Merge(
                listOf(
                    EProcessTemplate.params compose Every.map() compose everyQuantityRefInQuantityExpression,
                    EProcessTemplate.locals compose Every.map() compose everyQuantityRefInQuantityExpression,
                    EProcessTemplate.body compose everyQuantityRefInProcessExpression,
                )
            ),
            TemplateExpression.eProcessFinal.expression compose everyQuantityRefInProcessExpression,
        ),
    )

val everyQuantityRef: Every<Expression, EQuantityRef> =
    Merge(
        listOf(
            Expression.quantityExpression compose everyQuantityRefInQuantityExpression,
            Expression.lcaExpression compose everyQuantityRefInLcaExpression,
            Expression.templateExpression compose everyQuantityRefInTemplateExpression,
            Expression.systemExpression compose everyQuantityRefInSystemExpression,
        )
    )

val everyQuantityRefInEnvironment: PEvery<Environment, Environment, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            Environment.quantities compose everyRegister() compose everyQuantityRefInQuantityExpression,
            Environment.processTemplates compose everyRegister() compose
                    everyProcessTemplateInTemplateExpression compose
                    Merge(
                        listOf(
                            EProcessTemplate.params compose Every.map() compose everyQuantityRefInQuantityExpression,
                            EProcessTemplate.locals compose Every.map() compose everyQuantityRefInQuantityExpression,
                            EProcessTemplate.body compose everyQuantityRefInProcessExpression,
                        )
                    )
        )
    )
