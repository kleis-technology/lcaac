package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
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
            is PsiTechnoInputExchange -> checkTechnoInputExchange(element)
            is PsiTechnoProductExchange -> checkTechnoProductExchange(element)
            else -> throw IllegalArgumentException()
        }
    }

    private fun checkProcessArguments(element: PsiProcess): Map<String, TQuantity> {
        return rec.guard { el: PsiProcess ->
            el.getParameters().mapValues { checkQuantityExpression(it.value) }
        }(element)
    }

    private fun checkTechnoInputExchange(element: PsiTechnoInputExchange): TTechnoExchange {
        return rec.guard { el: PsiTechnoInputExchange ->
            val tyQuantity = checkQuantityExpression(el.getQuantity())
            val productName = el.getProductRef().name
            el.getProductRef().reference.resolve()?.let {
                val tyProductExchange = check(it)
                if (tyProductExchange !is TTechnoExchange) {
                    throw PsiTypeCheckException("expected TTechnoExchange, found $tyProductExchange")
                }
                if (tyProductExchange.product.dimension != tyQuantity.dimension) {
                    throw PsiTypeCheckException("incompatible dimensions: ${tyQuantity.dimension} vs ${tyProductExchange.product.dimension}")
                }
            }
            el.getFromProcessConstraint()?.let {
                val psiProcess = it.getProcessTemplateRef().reference.resolve() as PsiProcess?
                    ?: throw PsiTypeCheckException("unbound reference ${it.getProcessTemplateRef().name}")
                val tyArguments = checkProcessArguments(psiProcess)
                it.getArguments()
                    .forEach { (key, value) ->
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
            val tyQuantity = checkQuantityExpression(el.getAliasForField().getValue())
            TUnit(tyQuantity.dimension)
        }(element)
    }

    private fun checkUnitLiteral(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.getDimensionField().getValue()))
    }

    // Note: We do not guard against recursion here because our generated Parser is LR, contrary to the rest of the lang.
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
