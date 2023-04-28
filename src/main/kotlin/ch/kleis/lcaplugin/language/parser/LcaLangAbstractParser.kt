package ch.kleis.lcaplugin.language.parser

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Index
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyProcessTemplateInTemplateExpression
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.exchange.*
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.unit.*

class LcaLangAbstractParser(
    private val files: Sequence<LcaFile>,
) {
    fun load(): SymbolTable {
        val unitDefinitions = files.flatMap { it.getUnitDefinitions() }
        val processDefinitions = files.flatMap { it.getProcesses() }
        val substanceDefinitions = files.flatMap { it.getSubstances() }
        val globals: Register<QuantityExpression> = Register(Prelude.unitQuantities)
            .plus(
                unitDefinitions
                    .filter { it.getType() == UnitDefinitionType.LITERAL }
                    .map { it.getUnitRef().getUID().name to EQuantityLiteral(1.0, unitLiteral(it)) }
                    .asIterable()
            )
            .plus(
                unitDefinitions
                    .filter { it.getType() == UnitDefinitionType.ALIAS }
                    .map { it.getUnitRef().getUID().name to EQuantityLiteral(1.0, unitAlias(it)) }
                    .asIterable()
            )
            .plus(
                files
                    .flatMap { it.getGlobalAssignments() }
                    .map { it.first to quantity(it.second) }
                    .asIterable()
            )

        val units = Register(Prelude.units)
            .plus(
                unitDefinitions
                    .filter { it.getType() == UnitDefinitionType.LITERAL }
                    .map { Pair(it.getUnitRef().getUID().name, unitLiteral(it)) }
                    .asIterable()
            )
            .plus(
                unitDefinitions
                    .filter { it.getType() == UnitDefinitionType.ALIAS }
                    .map { Pair(it.getUnitRef().getUID().name, unitAlias(it)) }
                    .asIterable()
            )

        val processTemplates = Register.empty<ProcessTemplateExpression>()
            .plus(
                processDefinitions
                    .map { Pair(it.getProcessTemplateRef().getUID().name, process(it)) }
                    .asIterable()
            )

        val products = Register.empty<LcaUnconstrainedProductExpression>()
            .plus(
                processDefinitions
                    .flatMap { productsOf(it, globals, units) }
                    .map { Pair(it.name, it) }
                    .asIterable()
            )

        val processTemplatesIndexedByProduct = Index(processTemplates, Merge(
            listOf(
                everyProcessTemplateInTemplateExpression compose EProcessTemplate.body,
                ProcessTemplateExpression.eProcessFinal.expression,
            )
        ) compose
            EProcess.products compose
            Every.list() compose
            ETechnoExchange.product.product compose
            Merge(
                listOf(
                    LcaUnconstrainedProductExpression.eProduct.name,
                    LcaUnconstrainedProductExpression.eProductRef.name,
                )
            ))

        val substances = Register.empty<LcaSubstanceExpression>()
            .plus(
                substanceDefinitions
                    .map { Pair(it.getSubstanceRef().getUID().name, substance(it)) }
                    .asIterable()
            )

        val substanceCharacterizations = Register.empty<LcaSubstanceCharacterizationExpression>()
            .plus(
                substanceDefinitions
                    .filter { it.hasImpacts() }
                    .map { Pair(it.getSubstanceRef().getUID().name, substanceCharacterization(it)) }
                    .asIterable()
            )

        return SymbolTable(
            quantities = globals,
            products = products,
            processTemplates = processTemplates,
            templatesIndexedByProduct = processTemplatesIndexedByProduct,
            units = units,
            substances = substances,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    private fun substance(psiSubstance: PsiSubstance): LcaSubstanceExpression {
        return ESubstance(
            psiSubstance.getSubstanceRef().name,
            psiSubstance.getNameField().getValue(),
            SubstanceType.of(psiSubstance.getTypeField().getType()),
            psiSubstance.getCompartmentField().getValue(),
            psiSubstance.getSubcompartmentField()?.getValue(),
            unit(psiSubstance.getReferenceUnitField().getValue())
        )
    }

    private fun unitLiteral(psiUnitDefinition: PsiUnitDefinition): UnitExpression {
        return EUnitLiteral(
            psiUnitDefinition.getSymbolField().getValue(),
            1.0,
            Dimension.of(psiUnitDefinition.getDimensionField().getValue())
        )
    }

    private fun unitAlias(psiUnitAlias: PsiUnitDefinition): UnitExpression {
        return EUnitAlias(
            psiUnitAlias.getSymbolField().getValue(),
            quantity(psiUnitAlias.getAliasForField().getValue())
        )
    }

    private fun process(psiProcess: PsiProcess): ProcessTemplateExpression {
        val name = psiProcess.name
        val locals = psiProcess.getVariables().mapValues { quantity(it.value) }
        val params = psiProcess.getParameters().mapValues { quantity(it.value) }
        val products = generateProducts(psiProcess)
        val inputs = psiProcess.getInputs().map { technoInputExchange(it) }
        val emissions = psiProcess.getEmissions().map { bioExchange(it, Polarity.POSITIVE) }
        val landUse = psiProcess.getLandUse().map { bioExchange(it, Polarity.POSITIVE) }
        val resources = psiProcess.getResources().map { bioExchange(it, Polarity.NEGATIVE) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val body = EProcess(
            name = name,
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

    private fun generateProducts(psiProcess: PsiProcess): List<ETechnoExchange> {
        val productsWithoutAllocation = psiProcess.getProducts().map { technoProductExchange(it) }
        val productsWithAllocation = psiProcess.getProductsWithAllocation().map { technoProductExchangeWithAllocation(it) }
        return productsWithAllocation.plus(productsWithoutAllocation)
    }

    private fun productsOf(
        psiProcess: PsiProcess,
        globals: Register<QuantityExpression>,
        units: Register<UnitExpression>
    ): List<EProduct> {
        val locals = psiProcess.getVariables().mapValues { quantity(it.value) }
        val params = psiProcess.getParameters().mapValues { quantity(it.value) }
        val symbolTable = SymbolTable(
            quantities = Register(globals.plus(params).plus(locals)),
            units = Register(units),
        )
        return generateEProducts(psiProcess, symbolTable)
    }

    private fun generateEProducts(psiProcess: PsiProcess, symbolTable: SymbolTable): List<EProduct> {
        val productsWithoutAllocation = generateEProductsWithoutAllocation(psiProcess, symbolTable)
        val productsWithAllocation = generateEProductsWithAllocation(psiProcess, symbolTable)

        return productsWithoutAllocation.plus(productsWithAllocation)
    }

    private fun generateEProductsWithoutAllocation(psiProcess: PsiProcess, symbolTable: SymbolTable): List<EProduct> {
        return psiProcess.getProducts()
            .map {
                EProduct(
                    it.getProductRef().name,
                    EUnitClosure(symbolTable, EUnitOf(quantity(it.getQuantity())))
                )
            }
    }

    private fun generateEProductsWithAllocation(psiProcess: PsiProcess, symbolTable: SymbolTable): List<EProduct> {
        return psiProcess.getProductsWithAllocation()
            .map {
                EProduct(
                    it.getTechnoProductExchange().getProductRef().name,
                    EUnitClosure(symbolTable, EUnitOf(quantity(it.getTechnoProductExchange().getQuantity())))
                )
            }
    }

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization {
        val substanceRef = substanceRef(psiSubstance.getSubstanceRef().getUID().name)
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
        return EIndicatorRef(variable.name)
    }

    private fun technoInputExchange(psiExchange: PsiTechnoInputExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            constrainedProduct(
                psiExchange.getProductRef(),
                psiExchange.getFromProcessConstraint()
            )
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
                ref = it.getProcessTemplateRef().name,
                arguments = psiFromProcessConstraint.getArguments().mapValues { q -> quantity(q.value) },
            )
        } ?: None
    }

    private fun technoProductExchange(psiExchange: PsiTechnoProductExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            EConstrainedProduct(productRef(psiExchange.getProductRef()), None),
            EQuantityLiteral(100.0, EUnitLiteral("percent", 0.01, Dimension.None))
        )
    }

    private fun technoProductExchangeWithAllocation(psiExchange: PsiTechnoProductExchangeWithAllocateField): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getTechnoProductExchange().getQuantity()),
            EConstrainedProduct(productRef(psiExchange.getTechnoProductExchange().getProductRef()), None),
            quantity(psiExchange.getAllocateField().getValue())
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
        return ESubstanceRef(substance.name)
    }

    private fun substanceRef(name: String): LcaSubstanceExpression {
        return ESubstanceRef(name)
    }

    private fun productRef(product: PsiProductRef): LcaUnconstrainedProductExpression {
        return EProductRef(product.name)
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
            QuantityPrimitiveType.LITERAL -> EQuantityScale(
                primitive.getAmount(),
                quantityRef(primitive.getRef())
            )

            QuantityPrimitiveType.PAREN -> quantity(primitive.getQuantityInParen())
            QuantityPrimitiveType.QUANTITY_REF -> quantityRef(primitive.getRef())
        }
    }

    private fun quantityRef(variable: PsiQuantityRef): QuantityExpression {
        return EQuantityRef(variable.name)
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
            UnitPrimitiveType.DEFINITION -> unitLiteral(primitive.getDefinition())
            UnitPrimitiveType.PAREN -> unit(primitive.getUnitInParen())
            UnitPrimitiveType.VARIABLE -> unitRef(primitive.getRef())
        }
    }

    private fun unitRef(unitRef: PsiUnitRef): UnitExpression {
        return EUnitRef(unitRef.name)
    }
}

private enum class Polarity {
    POSITIVE, NEGATIVE
}
