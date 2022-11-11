package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.ElementaryExchange
import ch.kleis.lcaplugin.compute.model.IntermediaryExchange
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaVisitor
import com.intellij.psi.PsiElement
import ch.kleis.lcaplugin.compute.model.Process
import ch.kleis.lcaplugin.psi.LcaProduct
import ch.kleis.lcaplugin.psi.LcaProducts
import tech.units.indriya.quantity.Quantities.getQuantity
import java.lang.Double.parseDouble

class ModelVisitor : LcaVisitor() {
    private val processes = arrayListOf<Process>()
    private var processName: String = ""

    private val products = arrayListOf<IntermediaryExchange<*>>()


    private val inputs = arrayListOf<IntermediaryExchange<*>>()
    private val emissions = arrayListOf<ElementaryExchange<*>>()
    private val resources = arrayListOf<ElementaryExchange<*>>()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        element.acceptChildren(this)
    }

    override fun visitProcess(process: LcaProcess) {
        processName = process.name ?: ""
        process.processBody.productsList.forEach { visitProducts(it) }
        processes.add(Process(processName, products, inputs, emissions, resources))
    }

    override fun visitProducts(products: LcaProducts) {
        products.productList.forEach { visitProduct(it) }
    }

    override fun visitProduct(product: LcaProduct) {
        val name = product.name ?: throw IllegalArgumentException()
        val unitElement = product.getUnitElement() ?: throw IllegalArgumentException()
        val unit = unitElement.getQuantityUnit()
        val amount = parseDouble(product.number.text)
        val quantity = getQuantity(amount, unit)
        products.add(IntermediaryExchange(name, quantity))
    }


    fun get(): List<Process> {
        return processes
    }
}
