package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ProcessAnnotation
import ch.kleis.lcaac.core.lang.resolver.BareProcessResolver
import ch.kleis.lcaac.core.lang.resolver.CachedProcessResolver
import ch.kleis.lcaac.core.lang.resolver.ProcessTemplateResolver
import ch.kleis.lcaac.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaac.core.math.Operations
import com.mayakapps.kache.ObjectKache

class Oracle<Q, M>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: Operations<Q, M>,
    private val sourceOps: DataSourceOperations<Q>,
    private val cache: ObjectKache<Pair<EProcessTemplate<Q>, EProductSpec<Q>>, EProcess<Q>>
) {
    private val reduceDataExpressions = Reduce(symbolTable, ops, sourceOps)
    private val completeTerminals = CompleteTerminals(ops)
    private val processTemplateResolver = ProcessTemplateResolver(symbolTable)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(symbolTable)

    fun answer(ports: Set<Request<Q>>): Set<Response<Q>> {
        return ports.mapNotNull {
            answerRequest(it)
        }.toSet()
    }

    fun answerRequest(request: Request<Q>): Response<Q>? {
        return when (request) {
            is ProductRequest -> answerProductRequest(request)
            is SubstanceRequest -> answerSubstanceRequest(request)
        }
    }


    private fun answerProductRequest(request: ProductRequest<Q>): ProductResponse<Q>? {
        val spec = request.value
        val template = processTemplateResolver.resolve(spec) ?: return null

        val processResolver = if (template.annotations.contains(ProcessAnnotation.CACHED)) {
            CachedProcessResolver(symbolTable, ops, sourceOps, cache)
        } else {
            BareProcessResolver(symbolTable, ops, sourceOps)
        }

        val process = processResolver.resolve(template, spec)

        val selectedPortIndex = indexOf(request.value.name, process)
        return ProductResponse(request.address, process, selectedPortIndex)
    }

    private fun answerSubstanceRequest(request: SubstanceRequest<Q>): SubstanceResponse<Q>? {
        val spec = request.value
        return substanceCharacterizationResolver.resolve(spec)
            ?.takeIf { it.hasImpacts() }
            ?.let(reduceDataExpressions::apply)
            ?.let(completeTerminals::apply)
            ?.let { SubstanceResponse(request.address, it) }
    }

    private fun indexOf(productName: String, process: EProcess<Q>): Int {
        return process.products.indexOfFirst { it.product.name == productName }
    }
}
