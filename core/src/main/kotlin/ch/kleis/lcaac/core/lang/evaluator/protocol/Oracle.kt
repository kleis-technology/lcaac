package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.ProcessAnnotation
import ch.kleis.lcaac.core.lang.resolver.BareProcessResolver
import ch.kleis.lcaac.core.lang.resolver.CachedProcessResolver
import ch.kleis.lcaac.core.lang.resolver.ProcessTemplateResolver
import ch.kleis.lcaac.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaac.core.math.Operations
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import kotlinx.coroutines.runBlocking

interface Oracle<Q, M> {
    fun answer(ports: Set<Request<Q>>): Set<Response<Q>> {
        return ports.mapNotNull {
            answerRequest(it)
        }.toSet()
    }
    fun answerRequest(request: Request<Q>): Response<Q>?
}

class CachedOracle<Q, M>(
    private val inner: Oracle<Q, M>,
    private val cache: ObjectKache<Request<Q>, Response<Q>> = InMemoryKache(maxSize = 1024) {
        strategy = KacheStrategy.LRU
    }
) : Oracle<Q, M> {
    constructor(
        symbolTable: SymbolTable<Q>,
        ops: Operations<Q, M>,
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

class BareOracle<Q, M>(
    val symbolTable: SymbolTable<Q>,
    val ops: Operations<Q, M>,
    val sourceOps: DataSourceOperations<Q>,
): Oracle<Q, M> {
    private val reduceDataExpressions = Reduce(symbolTable, ops, sourceOps)
    private val completeTerminals = CompleteTerminals(ops)
    private val processTemplateResolver = ProcessTemplateResolver(symbolTable)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(symbolTable)

    override fun answerRequest(request: Request<Q>): Response<Q>? {
        return when (request) {
            is ProductRequest -> answerProductRequest(request)
            is SubstanceRequest -> answerSubstanceRequest(request)
        }
    }


    private fun answerProductRequest(request: ProductRequest<Q>): ProductResponse<Q>? {
        val spec = request.value
        val template = processTemplateResolver.resolve(spec) ?: return null

        val processResolver = if (template.annotations.contains(ProcessAnnotation.CACHED)) {
            CachedProcessResolver(symbolTable, ops, sourceOps)
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