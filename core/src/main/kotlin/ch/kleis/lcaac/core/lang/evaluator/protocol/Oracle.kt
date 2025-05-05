package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.resolver.ProcessResolver
import ch.kleis.lcaac.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaac.core.math.QuantityOperations
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import kotlinx.coroutines.runBlocking

interface Oracle<Q> {
    fun answer(ports: Set<Request<Q>>): Set<Response<Q>> {
        return ports.mapNotNull {
            answerRequest(it)
        }.toSet()
    }
    fun answerRequest(request: Request<Q>): Response<Q>?
}

class CachedOracle<Q>(
    private val inner: Oracle<Q>,
    private val cache: ObjectKache<Request<Q>, Response<Q>> = InMemoryKache(maxSize = 1024) {
        strategy = KacheStrategy.LRU
    }
) : Oracle<Q> {
    constructor(
        symbolTable: SymbolTable<Q>,
        ops: QuantityOperations<Q>,
        sourceOps: DataSourceOperations<Q>,
        cache: ObjectKache<Request<Q>, Response<Q>> = InMemoryKache(maxSize = 1024) {
            strategy = KacheStrategy.LRU
        }
    ): this(
        inner = BareOracle(symbolTable, ops, sourceOps),
        cache = cache,
    )

    override fun answerRequest(request: Request<Q>): Response<Q>? {
        return runBlocking {
            cache.getOrPut(request) {
                inner.answerRequest(request)
            }
        }
    }
}

class BareOracle<Q>(
    val symbolTable: SymbolTable<Q>,
    val ops: QuantityOperations<Q>,
    sourceOps: DataSourceOperations<Q>,
): Oracle<Q> {
    private val reduceDataExpressions = Reduce(symbolTable, ops, sourceOps)
    private val completeTerminals = CompleteTerminals(ops)
    private val processResolver = ProcessResolver(symbolTable)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(symbolTable)

    override fun answerRequest(request: Request<Q>): Response<Q>? {
        return when (request) {
            is ProductRequest -> answerProductRequest(request)
            is SubstanceRequest -> answerSubstanceRequest(request)
        }
    }

    private fun answerProductRequest(request: ProductRequest<Q>): ProductResponse<Q>? {
        val spec = request.value
        val template = processResolver.resolve(spec) ?: return null
        val arguments = template.params
            .plus(spec.fromProcess?.arguments ?: emptyMap())
        val expression = EProcessTemplateApplication(template, arguments)
        val process = expression
            .let(reduceDataExpressions::apply)
            .let(completeTerminals::apply)
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
