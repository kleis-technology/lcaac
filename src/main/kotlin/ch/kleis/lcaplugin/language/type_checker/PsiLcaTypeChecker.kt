package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.PsiElement

private class RecursiveGuard {
    private val visited = HashSet<PsiElement>()

    fun <E : PsiElement, R> guard(fn: (E) -> R): (E) -> R {
        return { element ->
            if (visited.contains(element)) {
                val names = visited.map {
                    """"${
                        it.text
                            .replace("\n", "")
                            .replace("\\s+".toRegex(), " ")
                            .take(20)
                    } ...""""
                }.sorted().joinToString().take(80)
                throw PsiTypeCheckException("circular dependencies: $names")
            }
            visited.add(element)
            val r = fn(element)
            visited.remove(element)
            r
        }
    }
}

class PsiLcaTypeChecker {
    private val rec = RecursiveGuard()

    fun check(element: PsiElement): Type {
        return when (element) {
            is PsiUnitDefinition -> checkUnit(element)
            is LcaQuantityExpression -> checkQuantityExpression(element)
            is PsiGlobalAssignment -> checkQuantityExpression(element.getValue())
            is PsiAssignment -> checkQuantityExpression(element.getValue())
            is LcaTechnoInputExchange -> checkTechnoInputExchange(element)
            is PsiTechnoProductExchange -> checkTechnoProductExchange(element)
            is LcaBioExchange -> checkBioExchange(element)
            else -> throw IllegalArgumentException("Uncheckable type: $element")
        }
    }

    private fun checkBioExchange(lcaBioExchange: LcaBioExchange): TBioExchange {
        return rec.guard { el: LcaBioExchange ->
            val tyQuantity = checkQuantityExpression(el.quantityExpression)
            val name = el.substanceSpec.name
            val comp = el.substanceSpec.getCompartmentField()?.getValue() ?: ""
            val subComp = el.substanceSpec.getSubCompartmentField()?.getValue()
            el.substanceSpec.reference.resolve()?.let {
                if (it is PsiSubstance) {
                    val tyRefQuantity = checkQuantityExpression(it.getReferenceUnitField().quantityExpression)
                    if (tyRefQuantity.dimension != tyQuantity.dimension) {
                        throw PsiTypeCheckException(
                            "Incompatible dimensions: substance reference dimension is ${tyRefQuantity.dimension} " +
                                "but exchange dimension is ${tyQuantity.dimension}"
                        )
                    }
                } else {
                    throw PsiTypeCheckException("Expected a PsiSubstance element but was ${it::class}")
                }
            }
            TBioExchange(TSubstance(name, tyQuantity.dimension, comp, subComp))
        }(lcaBioExchange)
    }

    private fun checkProcessArguments(element: PsiProcess): Map<String, TQuantity> {
        return rec.guard { el: PsiProcess ->
            el.getParameters().mapValues { checkQuantityExpression(it.value) }
        }(element)
    }

    private fun checkTechnoInputExchange(element: LcaTechnoInputExchange): TTechnoExchange {
        return rec.guard { el: LcaTechnoInputExchange ->
            val tyQuantity = checkQuantityExpression(el.quantityExpression)
            val productName = el.productRef.name
            el.productRef.reference.resolve()?.let {
                val tyProductExchange = check(it)
                if (tyProductExchange !is TTechnoExchange) {
                    throw PsiTypeCheckException("expected TTechnoExchange, found $tyProductExchange")
                }
                if (tyProductExchange.product.dimension != tyQuantity.dimension) {
                    throw PsiTypeCheckException("incompatible dimensions: ${tyQuantity.dimension} vs ${tyProductExchange.product.dimension}")
                }
            }
            el.fromProcessConstraint?.let {
                val psiProcess = it.processTemplateRef!!.reference.resolve() as PsiProcess?
                    ?: throw PsiTypeCheckException("unbound reference ${it.processTemplateRef!!.name}")
                val tyArguments = checkProcessArguments(psiProcess)
                it.argumentList
                    .forEach { arg ->
                        val key = arg.parameterRef.name
                        val value = arg.quantityExpression
                        val tyActual = checkQuantityExpression(value)
                        val tyExpected = tyArguments[key] ?: throw PsiTypeCheckException("unknown parameter $key")
                        if (tyExpected != tyActual) {
                            throw PsiTypeCheckException("incompatible dimensions: expecting ${tyExpected.dimension}, found ${tyActual.dimension}")
                        }
                    }
            }
            TTechnoExchange(TProduct(productName, tyQuantity.dimension))
        }(element)
    }


    private fun checkTechnoProductExchange(element: PsiTechnoProductExchange): TTechnoExchange {
        return rec.guard { el: PsiTechnoProductExchange ->
            val tyQuantity = checkQuantityExpression(el.getQuantity())
            val productName = el.getProductRef().name
            TTechnoExchange(TProduct(productName, tyQuantity.dimension))
        }(element)
    }

    private fun checkUnit(element: PsiUnitDefinition): TUnit {
        return when (element.getType()) {
            UnitDefinitionType.LITERAL -> checkUnitLiteral(element)
            UnitDefinitionType.ALIAS -> checkUnitAlias(element)
        }
    }

    private fun checkUnitAlias(element: PsiUnitDefinition): TUnit {
        return rec.guard { el: PsiUnitDefinition ->
            val tyQuantity = checkQuantityExpression(el.getAliasForField().quantityExpression)
            TUnit(tyQuantity.dimension)
        }(element)
    }

    private fun checkUnitLiteral(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.getDimensionField().getValue()))
    }

    private fun checkQuantityExpression(element: LcaQuantityExpression): TQuantity {
        return when (element) {
            is PsiQuantityRef -> TQuantity(checkDimensionOf(element))
            is LcaScaleQuantityExpression -> checkQuantityExpression(element.quantityExpression!!)
            is LcaParenQuantityExpression -> checkQuantityExpression(element.quantityExpression!!)
            is LcaExponentialQuantityExpression -> {
                val exponent = element.exponent.text.toDouble()
                val tyBase = checkQuantityExpression(element.quantityExpression)
                TQuantity(tyBase.dimension.pow(exponent))
            }

            is LcaBinaryOperatorExpression -> {
                val tyLeft = checkQuantityExpression(element.left)
                val tyRight = checkQuantityExpression(element.right!!)
                when (element) {
                    is LcaAddQuantityExpression, is LcaSubQuantityExpression -> {
                        if (tyLeft.dimension == tyRight.dimension) {
                            tyLeft
                        } else {
                            throw PsiTypeCheckException("incompatible dimensions: ${tyLeft.dimension} vs ${tyRight.dimension}")
                        }
                    }

                    is LcaMulQuantityExpression -> TQuantity(tyLeft.dimension.multiply(tyRight.dimension))
                    is LcaDivQuantityExpression -> TQuantity(tyLeft.dimension.divide(tyRight.dimension))
                    else -> throw PsiTypeCheckException("Unknown binary expression $element")
                }
            }

            else -> throw PsiTypeCheckException("Unknown expression $element")
        }
    }

    private fun checkDimensionOf(element: PsiQuantityRef): Dimension {
        return rec.guard { el: PsiQuantityRef ->
            el.reference.resolve()
                ?.let {
                    when (val ty = check(it)) {
                        is TQuantity -> ty.dimension
                        is TUnit -> ty.dimension
                        else -> throw PsiTypeCheckException("expected TQuantity or TUnit, found $ty")
                    }
                }
                ?: Prelude.unitMap[el.name]?.dimension
                ?: throw PsiTypeCheckException("unbound reference ${el.name}")
        }(element)
    }
}
