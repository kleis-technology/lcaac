package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import com.intellij.psi.PsiElement

private class RecursiveGuard {
    private val visited = ArrayList<PsiElement>()

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
                }.joinToString().take(80)
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
            is PsiQuantity -> checkQuantity(element)
            is PsiGlobalAssignment -> checkQuantity(element.getValue())
            is PsiAssignment -> checkQuantity(element.getValue())
            is PsiTechnoInputExchange -> checkTechnoInputExchange(element)
            is PsiTechnoProductExchange -> checkTechnoProductExchange(element)
            else -> throw IllegalArgumentException()
        }
    }

    private fun checkProcessArguments(element: PsiProcess): Map<String, TQuantity> {
        return rec.guard { el: PsiProcess ->
            el.getParameters().mapValues { checkQuantity(it.value) }
        }(element)
    }

    private fun checkTechnoInputExchange(element: PsiTechnoInputExchange): TTechnoExchange {
        return rec.guard { el: PsiTechnoInputExchange ->
            val tyQuantity = checkQuantity(el.getQuantity())
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
                        val tyActual = checkQuantity(value)
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
            val tyQuantity = checkQuantity(el.getQuantity())
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
            val tyQuantity = checkQuantity(el.getAliasForField().getValue())
            TUnit(tyQuantity.dimension)
        }(element)
    }

    private fun checkUnitLiteral(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.getDimensionField().getValue()))
    }

    private fun checkQuantity(element: PsiQuantity): TQuantity {
        return rec.guard { el: PsiQuantity ->
            val tyLeft = checkQuantityTerm(el.getTerm())
            when (el.getOperationType()) {
                AdditiveOperationType.ADD, AdditiveOperationType.SUB -> {
                    val tyRight = checkQuantity(el.getNext()!!)
                    if (tyLeft.dimension != tyRight.dimension) {
                        throw PsiTypeCheckException("incompatible dimensions: ${tyLeft.dimension} vs ${tyRight.dimension}")
                    }
                    tyLeft
                }

                null -> tyLeft
            }
        }(element)
    }

    private fun checkQuantityTerm(element: PsiQuantityTerm): TQuantity {
        return rec.guard { el: PsiQuantityTerm ->
            val tyLeft = checkQuantityFactor(el.getFactor())
            when (el.getOperationType()) {
                MultiplicativeOperationType.MUL -> {
                    val tyRight = checkQuantityTerm(el.getNext()!!)
                    TQuantity(tyLeft.dimension.multiply(tyRight.dimension))

                }

                MultiplicativeOperationType.DIV -> {
                    val tyRight = checkQuantityTerm(el.getNext()!!)
                    TQuantity(tyLeft.dimension.divide(tyRight.dimension))
                }

                null -> tyLeft
            }
        }(element)
    }

    private fun checkQuantityFactor(element: PsiQuantityFactor): TQuantity {
        return rec.guard { el: PsiQuantityFactor ->
            val tyPrimitive = checkQuantityPrimitive(el.getPrimitive())
            el
                .getExponent()
                ?.let { TQuantity(tyPrimitive.dimension.pow(it)) }
                ?: tyPrimitive
        }(element)
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

    private fun checkQuantityPrimitive(element: PsiQuantityPrimitive): TQuantity {
        return rec.guard { el: PsiQuantityPrimitive ->
            when (el.getType()) {
                QuantityPrimitiveType.LITERAL -> {
                    val dim = checkDimensionOf(el.getRef())
                    TQuantity(dim)
                }

                QuantityPrimitiveType.QUANTITY_REF -> {
                    val dim = checkDimensionOf(el.getRef())
                    TQuantity(dim)
                }

                QuantityPrimitiveType.PAREN -> checkQuantity(el.getQuantityInParen())
            }
        }(element)
    }
}
