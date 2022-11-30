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
    private var currentFile: LcaFile? = null
    private val processes = arrayListOf<UnitProcess>()
    private var products = arrayListOf<Exchange<*>>()
    private var inputs = arrayListOf<Exchange<*>>()
    private var emissions = arrayListOf<Exchange<*>>()
    private var resources = arrayListOf<Exchange<*>>()

    private var parameters = StaticVariableSet<Double>()
    private val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true) // support scientific notation

    private val externalDependencies = hashSetOf<LcaFile>()
    private val processedFiles = hashSetOf<LcaFile>()
    
    private val SUB_NS_PROCESSES = "processes"
    private val SUB_NS_FLOWS = "flows"

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element is LcaFile) {
            currentFile = element
            visitPsiPackage(element.getPackage())
            element.getImports().forEach { visitPsiImport(it) }
            element.getProcesses().forEach { visitPsiProcess(it) }
            element.getSubstances().forEach { visitPsiSubstance(it) }
            processedFiles.add(currentFile!!)
        }
        val next = externalDependencies.subtract(processedFiles)
        next.forEach { it.accept(this) }
    }

    private fun getCurrentPackageNs(): Namespace {
        val pkg = currentFile!!.getPackage()
        return Namespace.ROOT.ns(pkg.getUrnElement().getParts())
    }
    
    private fun getCurrentProcessNs() : Namespace {
        return getCurrentPackageNs().ns(SUB_NS_PROCESSES)
    }
    
    private fun getCurrentFlowNs() : Namespace {
        return getCurrentPackageNs().ns(SUB_NS_FLOWS)
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
            products.add(parseTechnoExchange(getCurrentFlowNs(), it))
        }

        inputs = arrayListOf()
        psiProcess.getInputExchanges().forEach {
            val pkg = addExternalDependency(it) ?: currentFile!!.getPackage()
            val pkgNs = Namespace.ROOT.ns(pkg.getUrnElement().getParts())
            inputs.add(parseTechnoExchange(pkgNs.ns(SUB_NS_FLOWS), it))
        }

        emissions = arrayListOf()
        psiProcess.getEmissionExchanges().forEach {
            val pkg = addExternalDependency(it) ?: currentFile!!.getPackage()
            val pkgNs = Namespace.ROOT.ns(pkg.getUrnElement().getParts())
            emissions.add(parseBioExchange(pkgNs.ns(SUB_NS_FLOWS), it))
        }

        resources = arrayListOf()
        psiProcess.getResourceExchanges().forEach {
            val pkg = addExternalDependency(it) ?: currentFile!!.getPackage()
            val pkgNs = Namespace.ROOT.ns(pkg.getUrnElement().getParts())
            resources.add(parseBioExchange(pkgNs.ns(SUB_NS_FLOWS), it))
        }

        processes.add(
            UnitProcess(
                getCurrentProcessNs().urn(processName),
                products,
                inputs + emissions + resources
            )
        )
    }

    private fun addExternalDependency(element: PsiElement): PsiPackage? {
        return element.reference?.resolve()?.containingFile?.let {
            if (it != currentFile) {
                externalDependencies.add(it as LcaFile)
            }
            return (it as LcaFile).getPackage()
        }
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
                getCurrentProcessNs().urn(name),
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
            Flow(getCurrentFlowNs().urn(name), unit),
            getQuantity(amount, unit)
        )
    }

    private fun <D : Quantity<D>> parseOutputExchange(substance: PsiSubstance): Exchange<D> {
        val name = substance.name ?: throw IllegalArgumentException()
        val unit = substance.getUnitElement().getUnit()
        val flow = Flow(getCurrentFlowNs().urn(name), unit) as Flow<D>
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
