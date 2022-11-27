package ch.kleis.lcaplugin.lib

import ch.kleis.lcaplugin.lib.model.Exchange
import ch.kleis.lcaplugin.lib.model.Flow
import ch.kleis.lcaplugin.lib.model.UnitProcess
import ch.kleis.lcaplugin.lib.registry.Namespace
import ch.kleis.lcaplugin.lib.system.CoreSystem
import ch.kleis.lcaplugin.psi.*
import com.fathzer.soft.javaluator.DoubleEvaluator
import com.fathzer.soft.javaluator.StaticVariableSet
import com.intellij.psi.PsiElement
import tech.units.indriya.AbstractUnit
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities.getQuantity
import java.lang.Double.parseDouble
import javax.measure.Quantity
import javax.measure.Unit

class ModelCoreSystemVisitor : LcaVisitor() {
    private var processName: String = ""
    private val processes = arrayListOf<UnitProcess>()

    private var products = arrayListOf<Exchange<*>>()

    private var inputs = arrayListOf<Exchange<*>>()
    private var emissions = arrayListOf<Exchange<*>>()
    private var resources = arrayListOf<Exchange<*>>()

    private var bioExchanges = arrayListOf<Exchange<*>>()
    private var parameters = StaticVariableSet<Double>()
    private val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true) // support scientific notation

    private val processesNs = Namespace.ROOT.ns("processes")
    private val flowsNs = Namespace.ROOT.ns("flows")

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        element.acceptChildren(this)
    }

    override fun visitProcess(process: LcaProcess) {
        processName = process.name ?: ""

        parameters = StaticVariableSet()
        process.processBody.parametersList.forEach { visitParameters(it) }

        products = arrayListOf()
        process.processBody.productsList.forEach { visitProducts(it) }
        inputs = arrayListOf()
        process.processBody.inputsList.forEach { visitInputs(it) }
        emissions = arrayListOf()
        process.processBody.emissionsList.forEach { visitEmissions(it) }
        resources = arrayListOf()
        process.processBody.resourcesList.forEach { visitResources(it) }
        processes.add(
            UnitProcess(
                processesNs.urn(processName),
                products,
                inputs + emissions + resources
            )
        )
    }

    override fun visitParameters(parameters: LcaParameters) {
        parameters.parameterList.forEach { visitParameter(it) }
    }

    override fun visitParameter(parameter: LcaParameter) {
        val name = parameter.name ?: throw IllegalStateException()
        val value = evaluator.evaluate(parameter.number.text)
        this.parameters.set(name, value)
    }

    override fun visitSubstance(substance: LcaSubstance) {
        val name = substance.name ?: throw IllegalArgumentException()
        val outputExchange = parseOutputExchangeOf(substance) as Exchange<*>
        inputs = arrayListOf()
        substance.substanceBody.factors?.factorList?.forEach { visitFactor(it) }
        processes.add(
            UnitProcess(
                processesNs.urn(name),
                listOf(outputExchange),
                inputs,
            )
        )
    }

    override fun visitFactor(factor: LcaFactor) {
        val unit = AbstractUnit.ONE
        val indicator = factor.uniqueId.name ?: throw IllegalArgumentException()
        val amount = parseDouble(factor.number.text)
        inputs.add(
            Exchange(
                Flow(flowsNs.urn(indicator), unit),
                getQuantity(amount, unit)
            )
        )
    }

    private fun <D : Quantity<D>> parseOutputExchangeOf(substance: LcaSubstance): Exchange<D> {
        val name = substance.name ?: throw IllegalArgumentException()
        val unit = substance.substanceBody.unitType.getUnitElement().getUnit()
        val flow = Flow(flowsNs.urn(name), unit) as Flow<D>
        val quantity = getQuantity(1.0, unit) as ComparableQuantity<D>
        return Exchange(flow, quantity)
    }

    override fun visitResources(resources: LcaResources) {
        bioExchanges = arrayListOf()
        resources.bioExchangeList.forEach { visitBioExchange(it) }
        this.resources.addAll(bioExchanges)
    }

    override fun visitEmissions(emissions: LcaEmissions) {
        bioExchanges.clear()
        emissions.bioExchangeList.forEach { visitBioExchange(it) }
        this.emissions.addAll(bioExchanges)
    }

    private fun <D : Quantity<D>> parseBioExchange(bioExchange: LcaBioExchange): Exchange<D> {
        val amount = evaluator.evaluate(bioExchange.fExpr.getContent(), this.parameters)
        val unit: Unit<D> = bioExchange.getUnitElement().getUnit() as Unit<D>
        val quantity = getQuantity(amount, unit)
        return Exchange(
            Flow(flowsNs.urn(bioExchange.name ?: throw IllegalStateException()), unit),
            quantity
        )
    }

    override fun visitBioExchange(bioExchange: LcaBioExchange) {
        val exchange = parseBioExchange(bioExchange) as Exchange<*>
        bioExchanges.add(exchange)
    }

    override fun visitProducts(products: LcaProducts) {
        products.productExchangeList.forEach { visitProductExchange(it) }
    }

    private fun <D : Quantity<D>> parseProductExchange(productExchange: LcaProductExchange): Exchange<D> {
        val unit = productExchange.getUnitElement().getUnit() as Unit<D>
        val name = productExchange.name ?: throw IllegalArgumentException()
        val flow = Flow(flowsNs.urn(name), unit)
        val amount = evaluator.evaluate(productExchange.fExpr.getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return Exchange(flow, quantity)
    }

    override fun visitProductExchange(productExchange: LcaProductExchange) {
        this.products.add(parseProductExchange(productExchange) as Exchange<*>)
    }

    override fun visitInputs(inputs: LcaInputs) {
        inputs.inputExchangeList.forEach { visitInputExchange(it) }
    }

    private fun <D : Quantity<D>> parseInputExchange(inputExchange: LcaInputExchange): Exchange<D> {
        val unit = inputExchange.getUnitElement().getUnit() as Unit<D>
        val flow = Flow(flowsNs.urn(inputExchange.name ?: throw IllegalArgumentException()), unit)
        val amount = evaluator.evaluate(inputExchange.fExpr.getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return Exchange(flow, quantity)
    }

    override fun visitInputExchange(inputExchange: LcaInputExchange) {
        inputs.add(parseInputExchange(inputExchange) as Exchange<*>)
    }

    fun getSystem(): CoreSystem {
        return CoreSystem(processes)
    }
}
