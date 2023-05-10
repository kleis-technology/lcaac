package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

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

                is EQuantityScale -> foldMap(M, source.quantity, map)
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

                is EQuantityScale -> EQuantityScale(
                    source.scale,
                    modify(source.quantity, map),
                )
            }
        }
    }

private val everyQuantityRefInConstraint: PEvery<FromProcessRef, FromProcessRef, EQuantityRef, QuantityExpression> =
    FromProcessRef.arguments compose
            Every.map() compose
            everyQuantityRefInQuantityExpression

private val everyQuantityRefInProductExpression: PEvery<EProductSpec, EProductSpec, EQuantityRef, QuantityExpression> =
    EProductSpec.fromProcessRef compose everyQuantityRefInConstraint

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

val everyQuantityRefInProcess: PEvery<EProcess, EProcess, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose everyQuantityRefInETechnoExchange,
            EProcess.inputs compose Every.list() compose everyQuantityRefInETechnoExchange,
            EProcess.biosphere compose Every.list() compose everyQuantityRefInEBioExchange,
        )
    )

private val everyQuantityRefInSubstanceCharacterization: PEvery<ESubstanceCharacterization, ESubstanceCharacterization, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            ESubstanceCharacterization.referenceExchange compose everyQuantityRefInEBioExchange,
            ESubstanceCharacterization.impacts compose Every.list() compose everyQuantityRefInEImpact,
        )
    )


private val everyQuantityRefInSystemExpression: PEvery<SystemExpression, SystemExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            SystemExpression.eSystem.processes compose
                    Every.list() compose
                    everyQuantityRefInProcess,
            SystemExpression.eSystem.substanceCharacterizations compose
                    Every.list() compose
                    everyQuantityRefInSubstanceCharacterization,
        )
    )

private val everyQuantityRefInLcaExpression: PEvery<LcaExpression, LcaExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            LcaExpression.eProcess compose
                    everyQuantityRefInProcess,
            LcaExpression.lcaExchangeExpression.eTechnoExchange compose
                    everyQuantityRefInETechnoExchange,
            LcaExpression.lcaExchangeExpression.eBioExchange compose
                    everyQuantityRefInEBioExchange,
            LcaExpression.eProductSpec.fromProcessRef.arguments compose
                    Every.map() compose everyQuantityRefInQuantityExpression
        )
    )

private val everyQuantityRefInTemplateExpression: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EQuantityRef, QuantityExpression> =
    Merge(
        listOf(
            everyProcessTemplateInTemplateExpression compose Merge(
                listOf(
                    EProcessTemplate.params compose Every.map() compose everyQuantityRefInQuantityExpression,
                    EProcessTemplate.locals compose Every.map() compose everyQuantityRefInQuantityExpression,
                    EProcessTemplate.body compose everyQuantityRefInProcess,
                )
            ),
            ProcessTemplateExpression.eProcessFinal.expression compose everyQuantityRefInProcess,
        ),
    )

val everyUnboundedQuantityRefInProcessTemplate =
    object : PEvery<EProcessTemplate, EProcessTemplate, EQuantityRef, QuantityExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: EProcessTemplate, map: (focus: EQuantityRef) -> R): R {
            val boundedRefs = source.params
                .plus(source.locals)
                .keys
                .map { EQuantityRef(it) }.toSet()
            val allRefs = everyQuantityRefInTemplateExpression.getAll(source).toSet()
            val unboundedRefs = allRefs.minus(boundedRefs).toList()
            return M.fold(
                unboundedRefs.map(map)
            )
        }

        override fun modify(
            source: EProcessTemplate,
            map: (focus: EQuantityRef) -> QuantityExpression
        ): EProcessTemplate {
            val boundedRefs = source.params
                .plus(source.locals)
                .keys
                .map { EQuantityRef(it) }.toSet()
            return EProcessTemplate(
                source.params,
                source.locals,
                everyQuantityRefInProcess.modify(source.body) {
                    if (boundedRefs.contains(it)) it
                    else map(it)
                }
            )
        }
    }

val everyUnboundedQuantityRefInTemplateExpression: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EQuantityRef, QuantityExpression> =
    ProcessTemplateExpression.eProcessTemplate compose everyUnboundedQuantityRefInProcessTemplate

val everyQuantityRef: Every<Expression, EQuantityRef> =
    Merge(
        listOf(
            Expression.quantityExpression compose everyQuantityRefInQuantityExpression,
            Expression.lcaExpression compose everyQuantityRefInLcaExpression,
            Expression.processTemplateExpression compose everyQuantityRefInTemplateExpression,
            Expression.systemExpression compose everyQuantityRefInSystemExpression,
        )
    )

