package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.*
import ch.kleis.lcaplugin.language.psi.mixin.StringLiteralMixin
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.PsiElement
import tech.units.indriya.quantity.Quantities.getQuantity
import java.lang.Double.parseDouble
import javax.measure.Quantity
import javax.measure.Unit

class ModelVisitor : LcaVisitor() {
    private val processes = arrayListOf<Process>()
    private var processName: String = ""

    private val products = arrayListOf<IntermediaryExchange<*>>()

    private val inputs = arrayListOf<IntermediaryExchange<*>>()
    private val emissions = arrayListOf<ElementaryExchange<*>>()
    private val resources = arrayListOf<ElementaryExchange<*>>()

    private val bioExchanges = arrayListOf<ElementaryExchange<*>>()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        element.acceptChildren(this)
    }

    override fun visitProcess(process: LcaProcess) {
        processName = process.name ?: ""
        products.clear()
        process.processBody.productsList.forEach { visitProducts(it) }
        inputs.clear()
        process.processBody.inputsList.forEach { visitInputs(it) }
        emissions.clear()
        process.processBody.emissionsList.forEach { visitEmissions(it) }
        resources.clear()
        process.processBody.resourcesList.forEach { visitResources(it) }
        processes.add(Process(processName, products, inputs, emissions, resources))
    }

    override fun visitResources(resources: LcaResources) {
        bioExchanges.clear()
        resources.bioExchangeList.forEach { visitBioExchange(it) }
        this.resources.addAll(bioExchanges)
    }

    override fun visitEmissions(emissions: LcaEmissions) {
        bioExchanges.clear()
        emissions.bioExchangeList.forEach { visitBioExchange(it) }
        this.emissions.addAll(bioExchanges)
    }

    private fun <D : Quantity<D>> parseBioExchange(bioExchange: LcaBioExchange): ElementaryExchange<D> {
        val amount = parseDouble(bioExchange.number.text)
        val unit: Unit<D> =
            (bioExchange.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()) as Unit<D>
        val quantity = getQuantity(amount, unit)

        val stringList = bioExchange.substanceId.stringLiteralList

        if (stringList.isEmpty()) {
            throw IllegalArgumentException()
        }

        val name = (stringList[0] as StringLiteralMixin).name ?: throw IllegalArgumentException()
        if (stringList.size <= 1) {
            return ElementaryExchange(
                ElementaryFlow(name, null, null, unit),
                quantity
            )
        }

        val compartment = (stringList[1] as StringLiteralMixin).name
        if (stringList.size <= 2) {
            return ElementaryExchange(
                ElementaryFlow(name, compartment, null, unit),
                quantity
            )
        }

        val subcompartment = (stringList[2] as StringLiteralMixin).name
        return ElementaryExchange(
            ElementaryFlow(name, compartment, subcompartment, unit),
            quantity
        )
    }

    override fun visitBioExchange(bioExchange: LcaBioExchange) {
        val exchange = parseBioExchange(bioExchange) as ElementaryExchange<*>
        bioExchanges.add(exchange)
    }

    override fun visitProducts(products: LcaProducts) {
        products.productList.forEach { visitProduct(it) }
    }

    private fun <D : Quantity<D>> parseProduct(product: LcaProduct): IntermediaryExchange<D> {
        val unit = (product.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()) as Unit<D>
        val name = product.name ?: throw IllegalArgumentException()
        val flow = IntermediaryFlow(name, unit)

        val amount = parseDouble(product.number.text)
        val quantity = getQuantity(amount, unit)
        return IntermediaryExchange(flow, quantity)
    }

    override fun visitProduct(product: LcaProduct) {
        this.products.add(parseProduct(product) as IntermediaryExchange<*>)
    }

    override fun visitInputs(inputs: LcaInputs) {
        inputs.inputExchangeList.forEach { visitInputExchange(it) }
    }

    private fun <D : Quantity<D>> parseInputExchange(inputExchange: LcaInputExchange): IntermediaryExchange<D> {
        val unit = (inputExchange.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()) as Unit<D>
        val flow = IntermediaryFlow(inputExchange.name ?: throw IllegalArgumentException(), unit)
        val amount = parseDouble(inputExchange.number.text)
        val quantity = getQuantity(amount, unit)
        return IntermediaryExchange(flow, quantity)
    }
    override fun visitInputExchange(inputExchange: LcaInputExchange) {
        inputs.add(parseInputExchange(inputExchange) as IntermediaryExchange<*>)
    }

    fun get(): System {
        return System(processes)
    }
}
