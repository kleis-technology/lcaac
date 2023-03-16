package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.unit.*

class LcaLangAbstractParser(
    private val files: List<LcaFile>,
) {
    fun load(): SymbolTable {
        val globals = Register.empty<QuantityExpression>()
        files
            .flatMap { it.getAssignments()}
            .forEach {
                globals[it.first] = quantity(it.second)
            }

        val units = Register(Prelude.units)
        files
            .flatMap { it.getUnitLiterals() }
            .filter { it.getUid() != null }
            .map { Pair(it.getUid()?.name!!, unitLiteral(it)) }
            .forEach {
                units[it.first] = it.second
            }

        val templates = Register.empty<TemplateExpression>()
        files
            .flatMap { it.getProcesses() }
            .filter { it.getUid() != null }
            .map { Pair(it.getUid()?.name!!, process(it)) }
            .forEach {
                templates[it.first] = it.second
            }

        val products = Register.empty<LcaUnconstrainedProductExpression>()
        files
            .flatMap { it.getProcesses() }
            .flatMap { productsOf(it, globals, units) }
            .map { Pair(it.name, it) }
            .forEach {
                products[it.first] = it.second
            }

        val substances = Register.empty<LcaSubstanceExpression>()
        files
            .flatMap { it.getSubstances() }
            .map { Pair(it.getUid().name, substance(it)) }
            .forEach {
                substances[it.first] = it.second
            }

        val substanceCharacterizations = Register.empty<LcaSubstanceCharacterizationExpression>()
        files
            .flatMap { it.getSubstances() }
            .filter { it.hasImpacts() }
            .map { Pair(it.getUid().name, substanceCharacterization(it)) }
            .forEach {
                substanceCharacterizations[it.first] = it.second
            }

        return SymbolTable(
            quantities = globals,
            products = products,
            processTemplates = templates,
            units = units,
            substances = substances,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    /*
        TODO: fill in compartment and subcompartment
     */
    private fun substance(psiSubstance: PsiSubstance): LcaSubstanceExpression {
        return ESubstance(
            psiSubstance.getUid().name,
            "IMPLEMENT ME",
            "IMPLEMENT ME",
            unit(psiSubstance.getReferenceUnitField().getValue())
        )
    }

    private fun unitLiteral(psiUnitLiteral: PsiUnitLiteral): UnitExpression {
        return EUnitLiteral(
            psiUnitLiteral.getSymbolField().getValue(),
            psiUnitLiteral.getScaleField().getValue(),
            Dimension.of(psiUnitLiteral.getDimensionField().getValue()),
        )
    }

    private fun process(psiProcess: PsiProcess): TemplateExpression {
        val locals = psiProcess.getVariables().mapValues { quantity(it.value) }
        val params = psiProcess.getParameters().mapValues { quantity(it.value) }
        val products = psiProcess.getProducts().map { technoProductExchange(it) }
        val inputs = psiProcess.getInputs().map { technoInputExchange(it) }
        val emissions = psiProcess.getEmissions().map { bioExchange(it, Polarity.POSITIVE) }
        val resources = psiProcess.getResources().map { bioExchange(it, Polarity.NEGATIVE) }
        val biosphere = emissions.plus(resources)
        val body = EProcess(
            products = products,
            inputs = inputs,
            biosphere = biosphere,
        )
        return EProcessTemplate(
            params,
            locals,
            body,
        )
    }

    private fun productsOf(
        psiProcess: PsiProcess,
        globals: Map<String, QuantityExpression>,
        units: Map<String, UnitExpression>
    ): List<EProduct> {
        val locals = psiProcess.getVariables().mapValues { quantity(it.value) }
        val params = psiProcess.getParameters().mapValues { quantity(it.value) }
        val symbolTable = SymbolTable(
            quantities = Register(globals.plus(params).plus(locals)),
            units = Register(units),
        )
        return psiProcess.getProducts()
            .map {
                EProduct(
                    it.getProductRef().name!!,
                    EUnitClosure(symbolTable, EUnitOf(quantity(it.getQuantity())))
                )
            }
    }

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization {
        val substanceRef = substanceRef(psiSubstance.getUid().name)
        val quantity = EQuantityLiteral(1.0, unit(psiSubstance.getReferenceUnitField().getValue()))
        val referenceExchange = EBioExchange(quantity, substanceRef)
        val impacts = psiSubstance.getImpactExchanges().map { impact(it) }

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun impact(exchange: PsiImpactExchange): EImpact {
        return EImpact(
            quantity(exchange.getQuantity()),
            indicatorRef(exchange.getIndicatorRef()),
        )
    }

    private fun indicatorRef(variable: PsiIndicatorRef): LcaIndicatorExpression {
        return EIndicatorRef(variable.name!!)
    }

    private fun technoInputExchange(psiExchange: PsiTechnoInputExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            constrainedProduct(
                psiExchange.getProductRef(),
                psiExchange.getFromProcessConstraint()
            ),
        )
    }

    private fun constrainedProduct(
        psiProductRef: PsiProductRef,
        psiFromProcessConstraint: PsiFromProcessConstraint?
    ): EConstrainedProduct {
        return EConstrainedProduct(
            productRef(psiProductRef),
            fromProcessConstraint(psiFromProcessConstraint),
        )
    }

    private fun fromProcessConstraint(psiFromProcessConstraint: PsiFromProcessConstraint?): Constraint {
        return psiFromProcessConstraint?.let {
            FromProcessRef(
                template = processTemplateRef(it.getProcessTemplateRef()),
                arguments = psiFromProcessConstraint.getArguments().mapValues { q -> quantity(q.value) },
            )
        } ?: None
    }

    private fun processTemplateRef(psiProcessTemplateRef: PsiProcessTemplateRef): ETemplateRef {
        return ETemplateRef(psiProcessTemplateRef.name!!)
    }

    private fun technoProductExchange(psiExchange: PsiTechnoProductExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            EConstrainedProduct(
                productRef(psiExchange.getProductRef()),
                None,
            )
        )
    }

    private fun bioExchange(psiExchange: PsiBioExchange, polarity: Polarity): EBioExchange {
        return when (polarity) {
            Polarity.POSITIVE -> EBioExchange(
                quantity(psiExchange.getQuantity()),
                substanceRef(psiExchange.getSubstanceRef()),
            )

            Polarity.NEGATIVE -> EBioExchange(
                EQuantityNeg(quantity(psiExchange.getQuantity())),
                substanceRef(psiExchange.getSubstanceRef()),
            )
        }
    }


    private fun substanceRef(substance: PsiSubstanceRef): LcaSubstanceExpression {
        return ESubstanceRef(substance.name!!)
    }

    private fun substanceRef(name: String): LcaSubstanceExpression {
        return ESubstanceRef(name)
    }

    private fun productRef(product: PsiProductRef): LcaUnconstrainedProductExpression {
        return EProductRef(product.name!!)
    }

    private fun quantity(quantity: PsiQuantity): QuantityExpression {
        val term = qTerm(quantity.getTerm())
        return when (quantity.getOperationType()) {
            AdditiveOperationType.ADD -> EQuantityAdd(
                term, quantity(quantity.getNext()!!)
            )

            AdditiveOperationType.SUB -> EQuantitySub(
                term, quantity(quantity.getNext()!!)
            )

            null -> term
        }
    }

    private fun qTerm(term: PsiQuantityTerm): QuantityExpression {
        val factor = qFactor(term.getFactor())
        return when (term.getOperationType()) {
            MultiplicativeOperationType.MUL -> EQuantityMul(
                factor, qTerm(term.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EQuantityDiv(
                factor, qTerm(term.getNext()!!)
            )

            null -> factor
        }
    }

    private fun qFactor(factor: PsiQuantityFactor): QuantityExpression {
        val primitive = qPrimitive(factor.getPrimitive())
        return factor.getExponent()?.let { EQuantityPow(primitive, it) }
            ?: primitive
    }

    private fun qPrimitive(primitive: PsiQuantityPrimitive): QuantityExpression {
        return when (primitive.getType()) {
            QuantityPrimitiveType.LITERAL -> EQuantityLiteral(
                primitive.getAmount(),
                unit(primitive.getUnit()),
            )

            QuantityPrimitiveType.PAREN -> quantity(primitive.getQuantityInParen())
            QuantityPrimitiveType.QUANTITY_REF -> quantityRef(primitive.getRef())
        }
    }

    private fun quantityRef(variable: PsiQuantityRef): QuantityExpression {
        return EQuantityRef(variable.name!!)
    }

    private fun unit(unit: PsiUnit): UnitExpression {
        val factor = uFactor(unit.getFactor())
        return when (unit.getOperationType()) {
            MultiplicativeOperationType.MUL -> EUnitMul(
                factor, unit(unit.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EUnitDiv(
                factor, unit(unit.getNext()!!),
            )

            null -> factor
        }
    }

    private fun uFactor(factor: PsiUnitFactor): UnitExpression {
        val primitive = uPrimitive(factor.getPrimitive())
        return factor.getExponent()?.let { EUnitPow(primitive, it) }
            ?: primitive
    }

    private fun uPrimitive(primitive: PsiUnitPrimitive): UnitExpression {
        return when (primitive.getType()) {
            UnitPrimitiveType.LITERAL -> unitLiteral(primitive.getLiteral())
            UnitPrimitiveType.PAREN -> unit(primitive.getUnitInParen())
            UnitPrimitiveType.VARIABLE -> unitRef(primitive.getRef())
        }
    }

    private fun unitRef(unitRef: PsiUnitRef): UnitExpression {
        return EUnitRef(unitRef.name!!)
    }
}

private enum class Polarity {
    POSITIVE, NEGATIVE
}
