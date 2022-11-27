package ch.kleis.lcaplugin.lib

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.lib.model.Exchange
import ch.kleis.lcaplugin.lib.model.Flow
import ch.kleis.lcaplugin.lib.model.UnitProcess
import ch.kleis.lcaplugin.lib.system.CoreSystem
import ch.kleis.lcaplugin.lib.urn.Namespace
import ch.kleis.lcaplugin.psi.LcaVisitor
import com.fathzer.soft.javaluator.DoubleEvaluator
import com.fathzer.soft.javaluator.StaticVariableSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import tech.units.indriya.AbstractUnit
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity
import javax.measure.Unit

class ModelCoreSystemVisitor : LcaVisitor() {
    private var processName: String = ""
    private val processes = arrayListOf<UnitProcess>()
    private var products = arrayListOf<Exchange<*>>()
    private var inputs = arrayListOf<Exchange<*>>()
    private var emissions = arrayListOf<Exchange<*>>()
    private var resources = arrayListOf<Exchange<*>>()

    private var parameters = StaticVariableSet<Double>()
    private val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true) // support scientific notation

    private val processesNs = Namespace.ROOT.ns("processes")
    private val flowsNs = Namespace.ROOT.ns("flows")

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element is LcaFile) {
            visitPsiPackage(element.getPackage())
            element.getImports().forEach { visitPsiImport(it) }
            element.getProcesses().forEach { visitPsiProcess(it) }
            element.getSubstances().forEach { visitPsiSubstance(it) }
        }
    }

    override fun visitPsiPackage(o: PsiPackage) {
        ProgressIndicatorProvider.checkCanceled()
        // implement me
    }

    override fun visitPsiImport(o: PsiImport) {
        ProgressIndicatorProvider.checkCanceled()
        // implement me
    }

    override fun visitPsiProcess(psiProcess: PsiProcess) {
        ProgressIndicatorProvider.checkCanceled()

        processName = psiProcess.name ?: throw IllegalStateException()

        parameters = StaticVariableSet()
        psiProcess.getParameters().forEach {
            val name = it.name ?: throw IllegalStateException()
            val value = it.getValue()
            parameters.set(name, value)
        }

        products = arrayListOf()
        psiProcess.getProductExchanges().forEach {
            products.add(parseTechnoExchange(it))
        }

        inputs = arrayListOf()
        psiProcess.getInputExchanges().forEach {
            inputs.add(parseTechnoExchange(it))
        }

        emissions = arrayListOf()
        psiProcess.getEmissionExchanges().forEach {
            emissions.add(parseBioExchange(it))
        }

        resources = arrayListOf()
        psiProcess.getResourceExchanges().forEach {
            resources.add(parseBioExchange(it))
        }

        processes.add(
            UnitProcess(
                processesNs.urn(processName),
                products,
                inputs + emissions + resources
            )
        )
    }


    override fun visitPsiSubstance(psiSubstance: PsiSubstance) {
        ProgressIndicatorProvider.checkCanceled()
        val name = psiSubstance.name ?: throw IllegalStateException()

        val output = parseOutputExchange(psiSubstance)

        inputs = arrayListOf()
        psiSubstance.getFactorExchanges().forEach {
            inputs.add(parsePsiFactorExchange(it))
        }

        processes.add(
            UnitProcess(
                processesNs.urn(name),
                listOf(output),
                inputs,
            )
        )
    }

    private fun parsePsiFactorExchange(exchange: PsiFactorExchange): Exchange<*> {
        val unit = AbstractUnit.ONE
        val name = exchange.name ?: throw IllegalArgumentException()
        val amount = exchange.getAmount()
        return Exchange(
            Flow(flowsNs.urn(name), unit),
            getQuantity(amount, unit)
        )
    }

    private fun <D : Quantity<D>> parseOutputExchange(substance: PsiSubstance): Exchange<D> {
        val name = substance.name ?: throw IllegalArgumentException()
        val unit = substance.getUnitElement().getUnit()
        val flow = Flow(flowsNs.urn(name), unit) as Flow<D>
        val quantity = getQuantity(1.0, unit) as ComparableQuantity<D>
        return Exchange(flow, quantity)
    }

    private fun <D : Quantity<D>> parseBioExchange(bioExchange: PsiBioExchange): Exchange<D> {
        val amount = evaluator.evaluate(bioExchange.getExpression().getContent(), this.parameters)
        val unit: Unit<D> = bioExchange.getUnitElement().getUnit() as Unit<D>
        val quantity = getQuantity(amount, unit)
        return Exchange(
            Flow(flowsNs.urn(bioExchange.name ?: throw IllegalStateException()), unit),
            quantity
        )
    }

    private fun <D : Quantity<D>> parseTechnoExchange(exchange: PsiTechnoExchange): Exchange<D> {
        val unit = exchange.getUnitElement().getUnit() as Unit<D>
        val name = exchange.name ?: throw IllegalArgumentException()
        val flow = Flow(flowsNs.urn(name), unit)
        val amount = evaluator.evaluate(exchange.getExpression().getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return Exchange(flow, quantity)
    }

    fun getSystem(): CoreSystem {
        return CoreSystem(processes)
    }
}
