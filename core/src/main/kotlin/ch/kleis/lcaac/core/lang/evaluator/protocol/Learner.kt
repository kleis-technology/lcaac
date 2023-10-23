package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

/*
    Start.
    The Learner starts with some initial requests. They represent the Learner's initial
    demand of products or substances. The Learner's goal is to learn the network of processes
    and substance characterizations required to satisfy this demand.

    Staging. The staging contains processes/substance characterizations waiting to be completed
    by an answer from the Oracle. These processes have inputs about which the Learner has asked
    the Oracle, but has not yet received an answer.

    Trace. Once the processes/substance characterizations in staging have been completed, the
    Learner commits them in one stage. The trace is the sequence of committed stages.

    Knowledge. This represents the association between a request (product or substance)
    and a response (process or substance characterization). Because the Oracle is assumed
    to be deterministic, the Learner does not need to ask the Oracle for a product/substance
    that she already knows, i.e., for which she already received an answer. This property
    guarantees that even if there is a potential loop in the network of processes,
    the Learner will eventually stop asking questions to the Oracle.

 */
class Learner<Q>(
    private val requests: Set<EProductSpec<Q>>,
    private val ops: QuantityOperations<Q>,
) {
    private val staging = Heap<ConnectionExpression<Q>>()
    private val start = Heap<PortExpression<Q>>()
    private val knowledge = HashMap<PortExpression<Q>, PortExpression<Q>?>()
    val trace = EvaluationTrace.empty<Q>()

    fun start(): Set<Request<Q>> {
        return requests.map {
            val index = start.add(it)
            ProductRequest(Address.virtual(index), it)
        }.toSet()
    }

    fun receive(responses: Set<Response<Q>>): Set<Request<Q>> {
        responses.forEach { updateStagingAndKnowledge(it) }
        commitStaging()

        val nextRequests = responses
            .let(this::applyKnowledge)
            .let(this::addToStaging)
            .let(this::nextRequests)
            .ifEmpty {
                commitStaging()
                emptySet()
            }
        return nextRequests
    }

    private fun applyKnowledge(process: EProcess<Q>): EProcess<Q> {
        return process.copy(
            inputs = process.inputs.map { exchange ->
                exchange.copy(
                    product = knowledge[exchange.product] as EProductSpec<Q>? ?: exchange.product
                )
            },
            biosphere = process.biosphere.map { exchange ->
                exchange.copy(
                    substance = knowledge[exchange.substance] as ESubstanceSpec<Q>? ?: exchange.substance
                )
            }
        )
    }

    private fun applyKnowledge(substanceCharacterization: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        return substanceCharacterization
    }

    private fun applyKnowledge(responses: Set<Response<Q>>): Set<ConnectionExpression<Q>> =
        responses.map { response ->
            when (response) {
                is ProductResponse -> applyKnowledge(response.value)
                is SubstanceResponse -> applyKnowledge(response.value)
            }
        }.toSet()

    private fun addToStaging(connections: Set<ConnectionExpression<Q>>): Set<Pair<ConnectionExpression<Q>, Int>> =
        connections.map { connection ->
            connection to staging.add(connection)
        }.toSet()

    private fun nextRequests(pairs: Set<Pair<ConnectionExpression<Q>, Int>>): Set<Request<Q>> =
        pairs.flatMap { (connection, index) ->
            when(connection) {
                is EProcess -> next(connection, index)
                is ESubstanceCharacterization -> emptySet()
            }
        }.filter { !knowledge.containsKey(it.value) }.toSet()

    private fun commitStaging() {
        staging.popAll().forEach { connection ->
            with(ToValue(ops)) {
                when (connection) {
                    is EProcess -> trace.add(connection.toValue())
                    is ESubstanceCharacterization -> trace.add(connection.toValue())
                }
            }
        }
        trace.commit()
    }

    private fun updateStagingAndKnowledge(response: Response<Q>) {
        return when (response) {
            is ProductResponse -> updateStagingAndKnowledgeWithProductResponse(response)
            is SubstanceResponse -> updateStagingAndKnowledgeWithSubstanceResponse(response)
        }
    }

    private fun updateStagingAndKnowledgeWithProductResponse(response: ProductResponse<Q>) {
        val address = response.address
        when (address.connectionIndex) {
            Heap.VIRTUAL_ADDRESS -> start.find(address.portIndex)
                ?.let { port ->
                    val process = response.value
                    val product = process.products[response.productInProcessIndex].product
                    knowledge[port] = product
                }

            else -> {
                val process = response.value
                val product = process.products[response.productInProcessIndex].product
                staging.find(address.connectionIndex)
                    ?.let { existingConnection ->
                        if (existingConnection is EProcess<Q>) {
                            knowledge[existingConnection.inputs[address.portIndex].product] = product
                        }
                    }
                staging.modify(
                    address.connectionIndex,
                    setProcessInputProduct(address.portIndex, product),
                )
            }
        }
    }

    private fun next(process: EProcess<Q>, connectionIndex: Int): Set<Request<Q>> {
        val productRequests =
            process.inputs.mapIndexed { portIndex, it ->
                ProductRequest(Address(connectionIndex, portIndex), it.product)
            }.toSet()
        val substanceRequest =
            process.biosphere.mapIndexed { portIndex, it ->
                SubstanceRequest(Address(connectionIndex, portIndex), it.substance)
            }.toSet()
        return productRequests + substanceRequest

    }

    private fun updateStagingAndKnowledgeWithSubstanceResponse(response: SubstanceResponse<Q>) {
        val address = response.address
        when (address.connectionIndex) {
            Heap.VIRTUAL_ADDRESS -> start.find(address.portIndex)?.let { port ->
                val substanceCharacterization = response.value
                knowledge[port] = substanceCharacterization.referenceExchange.substance
            }

            else -> {
                val substanceCharacterization = response.value
                val substance = substanceCharacterization.referenceExchange.substance
                staging.find(address.connectionIndex)
                    ?.let { existingConnection ->
                        if (existingConnection is EProcess<Q>) {
                            knowledge[existingConnection.biosphere[address.portIndex].substance] = substance
                        }
                    }
                staging.modify(
                    address.connectionIndex,
                    setProcessInputSubstance(address.portIndex, substance),
                )
            }
        }
    }

    private fun <Q> setProcessInputProduct(
        portIndex: Int,
        product: EProductSpec<Q>
    ): (ConnectionExpression<Q>) -> ConnectionExpression<Q> {
        return {
            when (it) {
                is EProcess -> it.copy(
                    inputs = it.inputs.mapIndexed { index, exchange ->
                        if (index == portIndex) exchange.copy(
                            product = product
                        )
                        else exchange
                    }
                )

                is ESubstanceCharacterization -> it
            }
        }
    }

    private fun <Q> setProcessInputSubstance(
        portIndex: Int,
        substance: ESubstanceSpec<Q>,
    ): (ConnectionExpression<Q>) -> ConnectionExpression<Q> {
        return {
            when (it) {
                is EProcess -> it.copy(
                    biosphere = it.biosphere.mapIndexed { index, exchange ->
                        if (index == portIndex) exchange.copy(
                            substance = substance
                        )
                        else exchange
                    }
                )

                is ESubstanceCharacterization -> it
            }
        }
    }
}
