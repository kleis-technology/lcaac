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

        val processTemplates = Register.empty<EProcessTemplate>()
            .plus(
                processDefinitions
                    .map { Pair(it.getProcessTemplateRef().getUID().name, process(it, globals, units)) }
                    .asIterable()
            )

        val substanceCharacterizations = Register.empty<ESubstanceCharacterization>()
            .plus(
                substanceDefinitions
                    .map { Pair(it.getSubstanceRef().getUID().name, substanceCharacterization(it)) }
                    .asIterable()
            )

        return SymbolTable(
            quantities = globals,
            processTemplates = processTemplates,
            units = units,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    private fun substance(psiSubstance: PsiSubstance): ESubstanceSpec {
        return ESubstanceSpec(
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

    private fun process(
        psiProcess: PsiProcess,
        globals: Register<QuantityExpression>,
        units: Register<UnitExpression>
    ): EProcessTemplate {
        val name = psiProcess.name
        val locals = psiProcess.getVariables().mapValues { quantity(it.value) }
        val params = psiProcess.getParameters().mapValues { quantity(it.value) }
        val symbolTable = SymbolTable(
            quantities = Register(globals.plus(params).plus(locals)),
            units = Register(units),
        )
        val products = generateTechnoProductExchanges(psiProcess, symbolTable)
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

    private fun generateTechnoProductExchanges(
        psiProcess: PsiProcess,
        symbolTable: SymbolTable,
    ): List<ETechnoExchange> {
        val productsWithoutAllocation = psiProcess.getProducts().map { technoProductExchange(it, symbolTable) }
        val productsWithAllocation =
            psiProcess.getProductsWithAllocation().map { technoProductExchangeWithAllocation(it, symbolTable) }
        return productsWithAllocation.plus(productsWithoutAllocation)
    }

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization {
        val substanceSpec = substanceSpec(psiSubstance)
        val quantity = EQuantityLiteral(1.0, unit(psiSubstance.getReferenceUnitField().getValue()))
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = psiSubstance.getImpactExchanges().map { impact(it) }

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun substanceSpec(psiSubstance: PsiSubstance): ESubstanceSpec {
        return ESubstanceSpec(
            name = psiSubstance.getSubstanceRef().name,
            displayName = psiSubstance.getNameField().getValue(),
            type = SubstanceType.of(psiSubstance.getTypeField().getType()),
            compartment = psiSubstance.getCompartmentField().getValue(),
            subcompartment = psiSubstance.getSubcompartmentField()?.getValue(),
            referenceUnit = unit(psiSubstance.getReferenceUnitField().getValue()),
        )
    }

    private fun impact(exchange: PsiImpactExchange): EImpact {
        return EImpact(
            quantity(exchange.getQuantity()),
            indicatorSpec(exchange.getIndicatorRef()),
        )
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec {
        return EIndicatorSpec(
            variable.name
        )
    }

    private fun technoInputExchange(psiExchange: PsiTechnoInputExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            productSpec(psiExchange.getProductRef(), psiExchange.getFromProcessConstraint()),
        )
    }

    private fun productSpec(
        psiProductRef: PsiProductRef,
        psiFromProcessConstraint: PsiFromProcessConstraint? = null,
    ): EProductSpec {
        return EProductSpec(
            psiProductRef.name,
            fromProcessRef = fromProcessRef(psiFromProcessConstraint),
        )
    }

    private fun fromProcessRef(psiFromProcessConstraint: PsiFromProcessConstraint?): FromProcessRef? {
        return psiFromProcessConstraint?.let {
            FromProcessRef(
                ref = it.getProcessTemplateRef().name,
                arguments = psiFromProcessConstraint.getArguments().mapValues { q -> quantity(q.value) },
            )
        }
    }

    private fun technoProductExchange(
        psiExchange: PsiTechnoProductExchange,
        symbolTable: SymbolTable
    ): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            productSpec(psiExchange.getProductRef())
                .withReferenceUnit(EUnitClosure(symbolTable, EUnitOf(quantity(psiExchange.getQuantity())))),
            EQuantityLiteral(100.0, EUnitLiteral("percent", 0.01, Dimension.None))
        )
    }

    private fun technoProductExchangeWithAllocation(
        psiExchange: PsiTechnoProductExchangeWithAllocateField,
        symbolTable: SymbolTable
    ): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getTechnoProductExchange().getQuantity()),
            productSpec(psiExchange.getTechnoProductExchange().getProductRef())
                .withReferenceUnit(EUnitClosure(symbolTable, EUnitOf(quantity(psiExchange.getTechnoProductExchange().getQuantity())))),
            quantity(psiExchange.getAllocateField().getValue())
        )
    }

    private fun bioExchange(psiExchange: PsiBioExchange, polarity: Polarity): EBioExchange {
        return when (polarity) {
            Polarity.POSITIVE -> EBioExchange(
                quantity(psiExchange.getQuantity()),
                substanceSpec(psiExchange.getSubstanceRef()),
            )

            Polarity.NEGATIVE -> EBioExchange(
                EQuantityNeg(quantity(psiExchange.getQuantity())),
                substanceSpec(psiExchange.getSubstanceRef()),
            )
        }
    }


    private fun substanceSpec(substance: PsiSubstanceRef): ESubstanceSpec {
        return ESubstanceSpec(
            name = substance.name,
            displayName = substance.name,
            compartment = null,
            subcompartment = null,
            type = null,
            referenceUnit = null,
        )
    }

    private fun substanceSpec(name: String): ESubstanceSpec {
        return ESubstanceSpec(
            name = name,
            displayName = name,
            compartment = null,
            subcompartment = null,
            type = null,
            referenceUnit = null,
        )
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
