package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.*
import ch.kleis.lcaplugin.language.psi.mixin.StringLiteralMixin
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.PsiElement
import tech.units.indriya.AbstractUnit.ONE
import tech.units.indriya.quantity.Quantities.getQuantity
import java.lang.Double.parseDouble
import javax.measure.Quantity
import javax.measure.Unit

class ModelMethodVisitor : LcaVisitor() {
    private var methodName: String = ""
    private val methodMap = HashMap<String, Method>()


    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        element.acceptChildren(this)
    }

    override fun visitProcess(process: LcaProcess) {
    }

    private fun <D : Quantity<D>> parseSubstance(substance: LcaSubstance): ElementaryExchange<D> {
        val stringList = substance.substanceId.stringLiteralList
        val name = (stringList.getOrNull(0) as StringLiteralMixin?)?.name ?: throw IllegalArgumentException()
        val compartment = (stringList.getOrNull(1) as StringLiteralMixin?)?.name
        val subcompartment = (stringList.getOrNull(2) as StringLiteralMixin?)?.name
        val unit = (substance.getUnitElement()?.getQuantityUnit() ?: throw IllegalArgumentException()) as Unit<D>
        return ElementaryExchange(
            ElementaryFlow(name, compartment, subcompartment, unit),
            getQuantity(1.0, unit),
        )
    }

    private fun parseIndicator(factor: LcaFactor): ImpactCategoryExchange {
        val name = (factor.stringLiteral as StringLiteralMixin).name ?: throw IllegalArgumentException()
        val amount = parseDouble(factor.number.text)
        return ImpactCategoryExchange(
            ImpactCategory(name),
            getQuantity(amount, ONE),
        )
    }

    override fun visitSubstance(substance: LcaSubstance) {
        val output = parseSubstance(substance)
        val factors = substance.substanceBody.factors ?: return

        methodName = factors.identifier.text
        val method = methodMap.getOrPut(methodName) { Method(methodName) }

        factors.factorList.forEach { factor ->
            val input = parseIndicator(factor)
            method.add(CharacterizationFactor(output, input))
        }
    }

    override fun visitResources(resources: LcaResources) {
    }

    override fun visitEmissions(emissions: LcaEmissions) {
    }

    override fun visitBioExchange(bioExchange: LcaBioExchange) {
    }

    override fun visitProducts(products: LcaProducts) {
    }

    override fun visitProduct(product: LcaProduct) {
    }

    override fun visitInputs(inputs: LcaInputs) {
    }

    override fun visitInputExchange(inputExchange: LcaInputExchange) {
    }

    fun getMethodMap(): Map<String, Method> {
        return methodMap
    }
}
