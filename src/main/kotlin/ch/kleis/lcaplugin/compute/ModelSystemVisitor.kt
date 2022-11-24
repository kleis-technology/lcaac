package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.*
import ch.kleis.lcaplugin.psi.*
import com.fathzer.soft.javaluator.DoubleEvaluator
import com.fathzer.soft.javaluator.StaticVariableSet
import com.intellij.psi.PsiElement
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity
import javax.measure.Unit

class ModelSystemVisitor : LcaVisitor() {
    private var processName: String = ""
    private val processes = arrayListOf<Process>()

    private var products = arrayListOf<IntermediaryExchange<*>>()

    private var inputs = arrayListOf<IntermediaryExchange<*>>()
    private var emissions = arrayListOf<ElementaryExchange<*>>()
    private var resources = arrayListOf<ElementaryExchange<*>>()

    private var bioExchanges = arrayListOf<ElementaryExchange<*>>()
    private var parameters = StaticVariableSet<Double>()
    private val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true) // support scientific notation

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
        processes.add(Process(processName, products, inputs, emissions, resources))
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

    private fun <D : Quantity<D>> parseBioExchange(bioExchange: LcaBioExchange): ElementaryExchange<D> {
        val amount = evaluator.evaluate(bioExchange.fExpr.getContent(), this.parameters)
        val unit: Unit<D> = bioExchange.getUnitElement().getUnit() as Unit<D>
        val quantity = getQuantity(amount, unit)
        return ElementaryExchange(
            ElementaryFlow(bioExchange.name ?: throw IllegalStateException(), unit),
            quantity
        )
    }

    override fun visitBioExchange(bioExchange: LcaBioExchange) {
        val exchange = parseBioExchange(bioExchange) as ElementaryExchange<*>
        bioExchanges.add(exchange)
    }

    override fun visitProducts(products: LcaProducts) {
        products.productExchangeList.forEach { visitProductExchange(it) }
    }

    private fun <D : Quantity<D>> parseProductExchange(productExchange: LcaProductExchange): IntermediaryExchange<D> {
        val unit = productExchange.getUnitElement().getUnit() as Unit<D>
        val name = productExchange.name ?: throw IllegalArgumentException()
        val flow = IntermediaryFlow(name, unit)
        val amount = evaluator.evaluate(productExchange.fExpr.getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return IntermediaryExchange(flow, quantity)
    }

    override fun visitProductExchange(productExchange: LcaProductExchange) {
        this.products.add(parseProductExchange(productExchange) as IntermediaryExchange<*>)
    }

    override fun visitInputs(inputs: LcaInputs) {
        inputs.inputExchangeList.forEach { visitInputExchange(it) }
    }

    private fun <D : Quantity<D>> parseInputExchange(inputExchange: LcaInputExchange): IntermediaryExchange<D> {
        val unit = inputExchange.getUnitElement().getUnit() as Unit<D>
        val flow = IntermediaryFlow(inputExchange.name ?: throw IllegalArgumentException(), unit)
        val amount = evaluator.evaluate(inputExchange.fExpr.getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return IntermediaryExchange(flow, quantity)
    }

    override fun visitInputExchange(inputExchange: LcaInputExchange) {
        inputs.add(parseInputExchange(inputExchange) as IntermediaryExchange<*>)
    }

    fun getSystem(): System {
        return System(processes)
    }
}
