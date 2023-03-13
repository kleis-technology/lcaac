package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.optics.PEvery
import arrow.optics.PPrism
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.processTemplates
import ch.kleis.lcaplugin.core.lang.substanceCharacterizations
import ch.kleis.lcaplugin.core.lang.substances

val substanceRefInLcaSubstanceExpression =
    object : PPrism<LcaSubstanceExpression, LcaSubstanceExpression, ESubstanceRef, LcaSubstanceExpression> {
        override fun getOrModify(source: LcaSubstanceExpression): Either<LcaSubstanceExpression, ESubstanceRef> {
            return when (source) {
                is ESubstance -> source.left()
                is ESubstanceRef -> source.right()
            }
        }

        override fun reverseGet(focus: LcaSubstanceExpression): LcaSubstanceExpression {
            return focus
        }
    }

private val everySubstanceRefInProcess: PEvery<EProcess, EProcess, ESubstanceRef, LcaSubstanceExpression> =
    EProcess.biosphere compose
            Every.list() compose
            EBioExchange.substance compose
            substanceRefInLcaSubstanceExpression

val everySubstanceRefInProcessExpression =
    LcaProcessExpression.eProcess compose everySubstanceRefInProcess

private val everySubstanceRefInSubstanceCharacterizationExpression =
    LcaSubstanceCharacterizationExpression.eSubstanceCharacterization.referenceExchange.substance compose
            substanceRefInLcaSubstanceExpression

private val everySubstanceRefInSystemExpression : PEvery<SystemExpression, SystemExpression, ESubstanceRef, LcaSubstanceExpression> =
    Merge(
        listOf(
            SystemExpression.eSystem.processes compose
                    Every.list() compose
                    everySubstanceRefInProcessExpression,
            SystemExpression.eSystem.substanceCharacterizations compose
                    Every.list() compose
                    everySubstanceRefInSubstanceCharacterizationExpression
        )
    )

val everySubstanceRef: Every<Expression, ESubstanceRef> =
    Merge(
        listOf(
            Expression.lcaExpression.lcaSubstanceExpression compose substanceRefInLcaSubstanceExpression,
            Expression.lcaExpression.lcaExchangeExpression.eBioExchange.substance compose substanceRefInLcaSubstanceExpression,
            Expression.lcaExpression.lcaProcessExpression.eProcess.biosphere compose
                    Every.list() compose EBioExchange.substance compose substanceRefInLcaSubstanceExpression,
            Expression.lcaExpression
                .lcaSubstanceCharacterizationExpression
                .eSubstanceCharacterization
                .referenceExchange
                .substance compose substanceRefInLcaSubstanceExpression,
            Expression.templateExpression compose
                    everyProcessTemplateInTemplateExpression compose
                    EProcessTemplate.body compose everySubstanceRefInProcessExpression,
            Expression.systemExpression compose everySubstanceRefInSystemExpression,
        )
    )

