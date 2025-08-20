package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.assessment.AnalysisProgram
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.Operations
import ch.kleis.lcaac.core.matrix.ImpactFactorMatrix
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import kotlinx.coroutines.runBlocking

interface ProcessResolver<Q, M> {
    fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q>
}

class CachedProcessResolver<Q, M>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: Operations<Q, M>,
    private val sourceOps: DataSourceOperations<Q>,
    private val cache: ObjectKache<Pair<EProcessTemplate<Q>, EProductSpec<Q>>, EProcess<Q>>
) : ProcessResolver<Q, M> {

    override fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q> {
        return runBlocking {
            cache.getOrPut(template to spec) {
                val trace = getTrace(template, spec)
                val entryPoint = trace.getEntryPoint()
                val analysis = AnalysisProgram(trace.getSystemValue(), entryPoint, ops).run()
                val inputQuantity = inputQuantityAnalysis(entryPoint.products, analysis.impactFactors)

                val inputs = analysis.impactFactors.getInputProducts().map {
                    EMapper.toETechnoExchange(inputQuantity(it), it)
                }

                val biosphere = analysis.impactFactors.getSubstances().map {
                    EMapper.toEBioExchange(inputQuantity(it), it)
                }

                val impacts = analysis.impactFactors.getIndicators().map {
                    EMapper.toEImpact(inputQuantity(it), it)
                }

                template.body.copy(
                    products = entryPoint.products.map { EMapper.toETechnoExchange(it) },
                    inputs = inputs.map { ETechnoBlockEntry(it) },
                    biosphere = biosphere.map { EBioBlockEntry(it) },
                    impacts = impacts.map { EImpactBlockEntry(it) }
                )
            } ?: throw EvaluatorException("Failed to resolve process for spec $spec")
        }
    }

    private fun getTrace(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EvaluationTrace<Q> {
        val arguments = template.params.plus(spec.fromProcess?.arguments ?: emptyMap())
        val newAnnotations = template.annotations.filter { it != ProcessAnnotation.CACHED }.toSet()
        val newTemplate = template.copy(annotations = newAnnotations)

        val newSymbolTable = symbolTable.copy(
            processTemplates = symbolTable.processTemplates.override(
                ProcessKey(newTemplate.body.name, newTemplate.body.labels.mapValues { it.value.value }),
                newTemplate
            )
        )
        val evaluator = Evaluator(newSymbolTable, ops, sourceOps)

        return evaluator.trace(newTemplate, arguments)
    }

    private fun inputQuantityAnalysis(
        products: List<TechnoExchangeValue<Q>>,
        impactFactors: ImpactFactorMatrix<Q, M>
    ): (MatrixColumnIndex<Q>) -> QuantityValue<Q> {
        return { inputPort: MatrixColumnIndex<Q> ->
            with(QuantityValueOperations(ops)) {
                products
                    .map { impactFactors.characterizationFactor(it.port(), inputPort) * it.quantity }
                    .reduce { a, b -> a + b }
            }
        }
    }
}

class BareProcessResolver<Q, M>(
    symbolTable: SymbolTable<Q>,
    ops: Operations<Q, M>,
    sourceOps: DataSourceOperations<Q>,
) : ProcessResolver<Q, M> {
    private val reduceDataExpressions = Reduce(symbolTable, ops, sourceOps)
    private val completeTerminals = CompleteTerminals(ops)

    override fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q> {
        val arguments = template.params.plus(spec.fromProcess?.arguments ?: emptyMap())
        val expression = EProcessTemplateApplication(template, arguments)
        return expression
            .let(reduceDataExpressions::apply)
            .let(completeTerminals::apply)
    }
}
