package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
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
            is LcaDataExpression -> checkDataExpression(element)
            is PsiGlobalAssignment -> checkDataExpression(element.getValue())
            is PsiAssignment -> checkDataExpression(element.getValue())
            is LcaTechnoInputExchange -> checkTechnoInputExchange(element)
            is PsiTechnoProductExchange -> checkTechnoProductExchange(element)
            is LcaBioExchange -> checkBioExchange(element)
            else -> throw PsiTypeCheckException("Uncheckable type: $element")
        }
    }

    private fun checkBioExchange(lcaBioExchange: LcaBioExchange): TBioExchange {
        return rec.guard { el: LcaBioExchange ->
            val tyQuantity = checkDataExpression(el.dataExpression, TQuantity::class.java)
            val name = el.substanceSpec.name
            val comp = el.substanceSpec.getCompartmentField()?.getValue() ?: ""
            val subComp = el.substanceSpec.getSubCompartmentField()?.getValue()
            el.substanceSpec.reference.resolve()?.let {
                if (it is PsiSubstance) {
                    val tyRefQuantity =
                        checkDataExpression(it.getReferenceUnitField().dataExpression, TQuantity::class.java)
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

    private fun checkProcessArguments(element: PsiProcess): Map<String, TypeDataExpression> {
        return rec.guard { el: PsiProcess ->
            el.getParameters().mapValues { checkDataExpression(it.value) }
        }(element)
    }

    private fun checkTechnoInputExchange(element: LcaTechnoInputExchange): TTechnoExchange {
        return rec.guard { el: LcaTechnoInputExchange ->
            val tyQuantity = checkDataExpression(el.dataExpression, TQuantity::class.java)
            val productName = el.inputProductSpec.name
            el.inputProductSpec.reference.resolve()?.let {
                val tyProductExchange = check(it)
                if (tyProductExchange !is TTechnoExchange) {
                    throw PsiTypeCheckException("expected TTechnoExchange, found $tyProductExchange")
                }
                if (tyProductExchange.product.dimension != tyQuantity.dimension) {
                    throw PsiTypeCheckException("incompatible dimensions: ${tyQuantity.dimension} vs ${tyProductExchange.product.dimension}")
                }
            }
            el.inputProductSpec.fromProcessConstraint?.let {
                val psiProcess = it.processTemplateSpec!!.reference.resolve() as PsiProcess?
                    ?: throw PsiTypeCheckException("unbound reference ${it.processTemplateSpec!!.name}")
                val tyArguments = checkProcessArguments(psiProcess)
                it.processTemplateSpec!!.argumentList
                    .forEach { arg ->
                        val key = arg.parameterRef.name
                        val value = arg.dataExpression
                        val tyActual = checkDataExpression(value)
                        val tyExpected = tyArguments[key] ?: throw PsiTypeCheckException("unknown parameter $key")
                        if (tyExpected != tyActual) {
                            throw PsiTypeCheckException("incompatible types: expecting $tyExpected, found $tyActual")
                        }
                    }
            }
            TTechnoExchange(TProduct(productName, tyQuantity.dimension))
        }(element)
    }


    private fun checkTechnoProductExchange(element: PsiTechnoProductExchange): TTechnoExchange {
        return rec.guard { el: PsiTechnoProductExchange ->
            val tyQuantity = checkDataExpression(el.getQuantity(), TQuantity::class.java)
            val productName = el.getOutputProductSpec().name
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
            val tyQuantity = checkDataExpression(el.getAliasForField().dataExpression, TQuantity::class.java)
            TUnit(tyQuantity.dimension)
        }(element)
    }

    private fun checkUnitLiteral(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.getDimensionField().getValue()))
    }

    private fun <T> checkDataExpression(element: LcaDataExpression, cls: Class<T>): T {
        val ty = checkDataExpression(element)
        if (ty.javaClass != cls) {
            throw PsiTypeCheckException("expected ${cls.simpleName}, found $ty")
        }
        return ty as T
    }

    private fun checkDataExpression(element: LcaDataExpression): TypeDataExpression {
        return when (element) {
            is PsiDataRef -> checkDataRef(element)
            is LcaStringExpression -> TString
            is LcaScaleQuantityExpression -> checkDataExpression(element.dataExpression!!)
            is LcaParenQuantityExpression -> checkDataExpression(element.dataExpression!!)
            is LcaExponentialQuantityExpression -> {
                val exponent = element.exponent.text.toDouble()
                val tyBase = checkDataExpression(element.dataExpression, TQuantity::class.java)
                TQuantity(tyBase.dimension.pow(exponent))
            }

            is LcaBinaryOperatorExpression -> {
                val tyLeft = checkDataExpression(element.left, TQuantity::class.java)
                val tyRight = checkDataExpression(element.right!!, TQuantity::class.java)
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

    private fun checkDataRef(element: PsiDataRef): TypeDataExpression {
        return rec.guard { el: PsiDataRef ->
            el.reference.resolve()
                ?.let {
                    when (val ty = check(it)) {
                        is TQuantity -> ty
                        is TUnit -> TQuantity(ty.dimension)
                        is TString -> ty
                        else -> throw PsiTypeCheckException("expected TQuantity, TUnit or TString, found $ty")
                    }
                }
                ?: Prelude.unitMap[el.name]?.let { TQuantity(it.dimension) }
                ?: throw PsiTypeCheckException("unbound reference ${el.name}")
        }(element)
    }
}
