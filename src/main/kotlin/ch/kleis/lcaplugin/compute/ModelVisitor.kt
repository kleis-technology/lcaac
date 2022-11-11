package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.ElementaryExchange
import ch.kleis.lcaplugin.compute.model.ElementaryFlow
import ch.kleis.lcaplugin.compute.model.IntermediaryExchange
import ch.kleis.lcaplugin.compute.model.Process
import ch.kleis.lcaplugin.language.psi.mixin.StringLiteralMixin
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.PsiElement
import tech.units.indriya.quantity.Quantities.getQuantity
import java.lang.Double.parseDouble

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

    override fun visitBioExchange(bioExchange: LcaBioExchange) {
        val amount = parseDouble(bioExchange.number.text)
        val unit = bioExchange.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()
        val quantity = getQuantity(amount, unit)

        val stringList = bioExchange.substanceId.stringLiteralList

        if (stringList.isEmpty()) {
            throw IllegalArgumentException()
        }

        val name = (stringList[0] as StringLiteralMixin).name ?: throw IllegalArgumentException()
        if (stringList.size <= 1) {
            bioExchanges.add(
                ElementaryExchange(
                    ElementaryFlow(name, null, null),
                    quantity
                )
            )
            return
        }

        val compartment = (stringList[1] as StringLiteralMixin).name
        if (stringList.size <= 2) {
            bioExchanges.add(
                ElementaryExchange(
                    ElementaryFlow(name, compartment, null),
                    quantity
                )
            )
            return
        }

        val subcompartment = (stringList[2] as StringLiteralMixin).name
        bioExchanges.add(
            ElementaryExchange(
                ElementaryFlow(name, compartment, subcompartment),
                quantity
            )
        )
    }

    override fun visitProducts(products: LcaProducts) {
        products.productList.forEach { visitProduct(it) }
    }

    override fun visitProduct(product: LcaProduct) {
        val name = product.name ?: throw IllegalArgumentException()
        val unit = product.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()
        val amount = parseDouble(product.number.text)
        val quantity = getQuantity(amount, unit)
        products.add(IntermediaryExchange(name, quantity))
    }

    override fun visitInputs(inputs: LcaInputs) {
        inputs.inputExchangeList.forEach { visitInputExchange(it) }
    }

    override fun visitInputExchange(inputExchange: LcaInputExchange) {
        val name = inputExchange.name ?: throw IllegalArgumentException()
        val unit = inputExchange.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()
        val amount = parseDouble(inputExchange.number.text)
        val quantity = getQuantity(amount, unit)
        inputs.add(IntermediaryExchange(name, quantity))
    }

    fun get(): List<Process> {
        return processes
    }
}
