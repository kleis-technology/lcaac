package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.compute.model.Exchange
import ch.kleis.lcaplugin.compute.model.Flow
import ch.kleis.lcaplugin.compute.model.UnitProcess
import ch.kleis.lcaplugin.compute.system.CoreSystem
import ch.kleis.lcaplugin.compute.urn.Namespace
import ch.kleis.lcaplugin.compute.urn.URN
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.psi.LcaVisitor
import com.fathzer.soft.javaluator.DoubleEvaluator
import com.fathzer.soft.javaluator.StaticVariableSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import org.apache.http.annotation.Experimental
import tech.units.indriya.AbstractUnit
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity
import javax.measure.Unit

@Experimental
class ModelCoreSystemVisitor : LcaVisitor() {
    private var processName: String = ""
    private val processes = arrayListOf<UnitProcess>()
    private var products = arrayListOf<Exchange<*>>()
    private var inputs = arrayListOf<Exchange<*>>()
    private var emissions = arrayListOf<Exchange<*>>()
    private var resources = arrayListOf<Exchange<*>>()

    private var parameters = StaticVariableSet<Double>()
    private val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true) // support scientific notation

    private val externalDependencies = HashMap<URN, PsiElement>()
    private val processedElements = HashSet<URN>()

    private val SUB_NS_PROCESSES = "processes"
    private val SUB_NS_FLOWS = "flows"

    private var previousHashCode: Int = 0

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element is LcaFile) {
            visitPsiPackage(element.getPackage())
            element.getImports().forEach { visitPsiImport(it) }
            element.getProcesses().forEach { visitPsiProcess(it) }
            element.getSubstances().forEach { visitPsiSubstance(it) }
        }
    }

    private fun visitDependencies() {
        val next = externalDependencies.entries
            .filter { !processedElements.contains(it.key) }
            .map { it.value }

        val nextHashcode = next.sumOf { it.hashCode() } // prevents infinite loop
        if (previousHashCode != nextHashcode) {
            previousHashCode = nextHashcode
            next.forEach {
                it.accept(this)
            }
        }
    }

    private fun getPackageOf(element: PsiElement): PsiPackage {
        val lcaFile = element.containingFile as LcaFile
        return lcaFile.getPackage()
    }

    private fun getPackageNamespaceOf(element: PsiElement): Namespace {
        return Namespace.ROOT.ns(getPackageOf(element).getUrnElement().getParts())
    }

    private fun getCurrentPackageNs(element: PsiElement): Namespace {
        val pkg = getPackageOf(element)
        return Namespace.ROOT.ns(pkg.getUrnElement().getParts())
    }

    private fun getCurrentProcessNs(element: PsiElement): Namespace {
        return getCurrentPackageNs(element).ns(SUB_NS_PROCESSES)
    }

    private fun getCurrentFlowNs(element: PsiElement): Namespace {
        return getCurrentPackageNs(element).ns(SUB_NS_FLOWS)
    }

    override fun visitPsiPackage(o: PsiPackage) {
        ProgressIndicatorProvider.checkCanceled()
    }

    override fun visitPsiImport(o: PsiImport) {
        ProgressIndicatorProvider.checkCanceled()
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
            val exchange = parseTechnoExchange(getCurrentFlowNs(psiProcess), it)
            products.add(exchange)
            processedElements.add(exchange.flow.getUrn())
        }

        inputs = arrayListOf()

        psiProcess.getInputExchanges().forEach {
            val dependency = (it.reference?.resolve() ?: it) as PsiTechnoExchange
            val pkgNs = getPackageNamespaceOf(dependency)
            val exchange = parseTechnoExchange(pkgNs.ns(SUB_NS_FLOWS), it)
            inputs.add(exchange)
            externalDependencies[exchange.flow.getUrn()] = dependency.getContainingProcess()
        }

        emissions = arrayListOf()
        psiProcess.getEmissionExchanges().forEach {
            val dependency = it.reference?.resolve() ?: it
            val pkgNs = getPackageNamespaceOf(dependency)
            val exchange = parseBioExchange(pkgNs.ns(SUB_NS_FLOWS), it)
            emissions.add(exchange)
            externalDependencies[exchange.flow.getUrn()] = dependency
        }

        resources = arrayListOf()
        psiProcess.getResourceExchanges().forEach {
            val dependency = it.reference?.resolve() ?: it
            val pkgNs = getPackageNamespaceOf(dependency)
            val exchange = parseBioExchange(pkgNs.ns(SUB_NS_FLOWS), it)
            emissions.add(exchange)
            externalDependencies[exchange.flow.getUrn()] = dependency
        }

        val process = UnitProcess(
            getCurrentProcessNs(psiProcess).urn(processName),
            products,
            inputs + emissions + resources
        )
        processes.add(process)
        processedElements.add(process.getUrn())

        visitDependencies()
    }

    override fun visitPsiSubstance(psiSubstance: PsiSubstance) {
        ProgressIndicatorProvider.checkCanceled()
        val name = psiSubstance.name ?: throw IllegalStateException()

        val output = parseOutputExchange(psiSubstance)
        processedElements.add(output.flow.getUrn())

        inputs = arrayListOf()
        psiSubstance.getFactorExchanges().forEach {
            val exchange = parsePsiFactorExchange(it)
            inputs.add(exchange)
            processedElements.add(exchange.flow.getUrn())
        }

        val process = UnitProcess(
            getCurrentProcessNs(psiSubstance).urn(name),
            listOf(output),
            inputs,
        )
        processes.add(process)
        processedElements.add(process.getUrn())
    }

    private fun parsePsiFactorExchange(exchange: PsiFactorExchange): Exchange<*> {
        val unit = AbstractUnit.ONE
        val name = exchange.name ?: throw IllegalArgumentException()
        val amount = exchange.getAmount()
        return Exchange(
            Flow(getCurrentFlowNs(exchange).urn(name), unit),
            getQuantity(amount, unit)
        )
    }

    private fun <D : Quantity<D>> parseOutputExchange(substance: PsiSubstance): Exchange<D> {
        val name = substance.name ?: throw IllegalArgumentException()
        val unit = substance.getUnitElement().getUnit()
        val flow = Flow(getCurrentFlowNs(substance).urn(name), unit) as Flow<D>
        val quantity = getQuantity(1.0, unit) as ComparableQuantity<D>
        return Exchange(flow, quantity)
    }

    private fun <D : Quantity<D>> parseBioExchange(parentNs: Namespace, bioExchange: PsiBioExchange): Exchange<D> {
        val amount = evaluator.evaluate(bioExchange.getExpression().getContent(), this.parameters)
        val unit: Unit<D> = bioExchange.getUnitElement().getUnit() as Unit<D>
        val quantity = getQuantity(amount, unit)
        return Exchange(
            Flow(parentNs.urn(bioExchange.name ?: throw IllegalStateException()), unit),
            quantity
        )
    }

    private fun <D : Quantity<D>> parseTechnoExchange(parentNs: Namespace, exchange: PsiTechnoExchange): Exchange<D> {
        val unit = exchange.getUnitElement().getUnit() as Unit<D>
        val name = exchange.name ?: throw IllegalArgumentException()
        val flow = Flow(parentNs.urn(name), unit)
        val amount = evaluator.evaluate(exchange.getExpression().getContent(), this.parameters)
        val quantity = getQuantity(amount, unit)
        return Exchange(flow, quantity)
    }

    fun getSystem(): CoreSystem {
        return CoreSystem(processes)
    }
}
