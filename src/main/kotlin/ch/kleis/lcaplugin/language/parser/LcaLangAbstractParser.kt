package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.unit.*

class LcaLangAbstractParser(
    private val findFilesOf: (String) -> List<LcaFile>,
) {
    fun collect(pkgName: String): Pair<Package, List<Package>> {
        val visited = HashSet<String>()
        val pkg = Prelude.packages[pkgName] ?: mkPackage(pkgName)
        visited.add(pkgName)
        val dependencies = pkg.imports
            .flatMap {
                val deps = collectDependencies(it.pkgName, visited)
                visited.addAll(deps.map { dep -> dep.name })
                deps
            }
        return Pair(pkg, dependencies)
    }

    private fun collectDependencies(pkgName: String, visited: HashSet<String>): List<Package> {
        if (visited.contains(pkgName)) {
            return emptyList()
        }
        val pkg = Prelude.packages[pkgName] ?: mkPackage(pkgName)
        if (pkg.imports.isEmpty()) {
            return listOf(pkg)
        }
        val dependencies = pkg.imports
            .flatMap {
                val deps = collectDependencies(it.pkgName, visited)
                visited.addAll(deps.map { dep -> dep.name })
                deps
            }
        return dependencies.plus(pkg)
    }

    private fun mkPackage(pkgName: String): Package {
        val files = findFilesOf(pkgName)
        val globals = files
            .flatMap { it.getAssignments() }
            .associate { Pair(it.getUid().name!!, quantity(it.getCoreExpression().asQuantity())) }

        val templates = files
            .flatMap { it.getProcesses() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, process(it)) }

        val substances = files
            .flatMap { it.getSubstances() }
            .associate { Pair(it.getUid().name!!, substance(it)) }

        val substanceCharacterizations = files
            .flatMap { it.getSubstances() }
            .filter { it.hasEmissionFactors() }
            .associate { Pair(it.getUid().name!!, substanceCharacterization(it)) }

        val units = files
            .flatMap { it.getUnitLiterals() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, unitLiteral(it)) }


        val environment = Environment(
            quantities = Register(globals),
            processTemplates = Register(templates),
            units = Register(units),
            substances = Register(substances),
            substanceCharacterizations = Register(substanceCharacterizations)
        )

        val imports = files
            .flatMap { it.getImports() }
            .map {
                when (it.getImportType()) {
                    ImportType.SYMBOL -> ImportSymbol(it.getPackageName(), it.getSymbol()!!)
                    ImportType.WILDCARD -> ImportWildCard(it.getPackageName())
                }
            }

        return Package(
            pkgName,
            imports,
            environment,
        )
    }

    /*
        TODO: fill in compartment and subcompartment
     */
    private fun substance(psiSubstance: PsiSubstance): LcaSubstanceExpression {
        return ESubstance(
            psiSubstance.getUid().name!!,
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

    private fun locals(assignments: Collection<PsiAssignment>): Map<String, QuantityExpression> {
        return assignments
            .associate { Pair(it.getUid().name!!, quantity(it.getCoreExpression().asQuantity())) }
    }

    private fun params(parameters: Collection<PsiParameter>): Map<String, QuantityExpression> {
        return parameters
            .associate {
                Pair(
                    it.getUid().name!!,
                    it.getCoreExpression()?.asQuantity()?.let { e -> quantity(e) }!!
                )
            }
    }

    private fun process(psiProcess: PsiProcess): TemplateExpression {
        val locals = locals(psiProcess.getLocalAssignments())
        val params = params(psiProcess.getParameters())
        val products = psiProcess.getBlocks()
            .filter { it.getType() == BlockType.PRODUCTS }
            .flatMap { technoBlock(it) }
        val inputs = psiProcess.getBlocks()
            .filter { it.getType() == BlockType.INPUTS }
            .flatMap { technoBlock(it) }
        val biosphere = psiProcess.getBlocks()
            .filter { it.getType() == BlockType.EMISSIONS || it.getType() == BlockType.RESOURCES }
            .flatMap { bioBlock(it) }
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

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization {
        val substanceRef = substanceRef(psiSubstance.getUid().name!!)
        val quantity = EQuantityLiteral(1.0, unit(psiSubstance.getReferenceUnitField().getValue()))
        val referenceExchange = EBioExchange(quantity, substanceRef)
        val impacts = psiSubstance.getEmissionFactors()
            ?.getExchanges()
            ?.map { impact(it) }
            ?: emptyList()

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun impact(exchange: PsiExchange): EImpact {
        return EImpact(
            quantity(exchange.getQuantity()),
            indicatorRef(exchange.getProduct()),
        )
    }

    private fun indicatorRef(variable: PsiVariable): LcaIndicatorExpression {
        return EIndicatorRef(variable.name!!)
    }

    private fun technoExchange(psiExchange: PsiExchange): ETechnoExchange {
        return ETechnoExchange(
            quantity(psiExchange.getQuantity()),
            productRef(psiExchange.getProduct()),
        )
    }

    private fun bioExchange(psiExchange: PsiExchange, polarity: Polarity): EBioExchange {
        return when (polarity) {
            Polarity.POSITIVE -> EBioExchange(
                quantity(psiExchange.getQuantity()),
                substanceRef(psiExchange.getProduct()),
            )

            Polarity.NEGATIVE -> EBioExchange(
                EQuantityNeg(quantity(psiExchange.getQuantity())),
                substanceRef(psiExchange.getProduct()),
            )
        }
    }


    private fun substanceRef(substance: PsiVariable): LcaSubstanceExpression {
        return ESubstanceRef(substance.name!!)
    }

    private fun substanceRef(name: String): LcaSubstanceExpression {
        return ESubstanceRef(name)
    }

    private fun productRef(product: PsiVariable): LcaProductExpression {
        return EProductRef(product.name!!)
    }

    private fun technoBlock(psiBlock: PsiBlock): List<ETechnoExchange> {
        return psiBlock.getExchanges()
            .map { technoExchange(it) }
    }

    private fun bioBlock(psiBlock: PsiBlock): List<EBioExchange> {
        return psiBlock.getExchanges()
            .map { bioExchange(it, psiBlock.getPolarity()) }
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
                primitive.getAmount()!!,
                unit(primitive.getUnit()!!),
            )

            QuantityPrimitiveType.PAREN -> quantity(primitive.getQuantityInParen()!!)
            QuantityPrimitiveType.VARIABLE -> quantityRef(primitive.getVariable()!!)
        }
    }

    private fun quantityRef(variable: PsiVariable): QuantityExpression {
        return EQuantityRef(variable.name!!)
    }

    private fun unit(unit: PsiUnit): UnitExpression {
        val factor = uFactor(unit.getFactor())
        return when (unit.getOperationType()) {
            MultiplicativeOperationType.MUL -> EUnitMul(
                factor, unit(unit.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EUnitMul(
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
            UnitPrimitiveType.LITERAL -> unitLiteral(primitive.asLiteral()!!)
            UnitPrimitiveType.PAREN -> unit(primitive.asUnitInParen()!!)
            UnitPrimitiveType.VARIABLE -> unitRef(primitive.asVariable()!!)
        }
    }

    private fun unitRef(variable: PsiVariable): UnitExpression {
        return EUnitRef(variable.name!!)
    }
}
