package ch.kleis.lcaac.core.lang.evaluator.arena

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class Proponent<Q>(
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
        val nextRequests = nextRequests(responses)
        if (nextRequests.isEmpty()) {
            commitStaging()
        }
        return nextRequests
    }

    private fun applyKnowledgeTo(process: EProcess<Q>): EProcess<Q> {
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
    private fun applyKnowledgeTo(substanceCharacterization: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        return substanceCharacterization
    }

    private fun nextRequests(responses: Set<Response<Q>>) =
        responses.flatMap { response ->
            when (response) {
                is ProductResponse -> {
                    val process = applyKnowledgeTo(response.value)
                    val connectionIndex = staging.add(process)
                    next(process, connectionIndex)
                }

                is SubstanceResponse -> {
                    val substanceCharacterization = applyKnowledgeTo(response.value)
                    staging.add(substanceCharacterization)
                    emptySet()
                }
            }
        }.filter { !knowledge.containsKey(it.value) }.toSet()

    private fun commitStaging() {
        staging.popAll().forEach { connection ->
            with(ToValue(ops)) {
                when(connection) {
                    is EProcess -> trace.add(connection.toValue())
                    is ESubstanceCharacterization -> trace.add(connection.toValue())
                }
            }
        }
        trace.commit()
    }

    private fun updateStagingAndKnowledge(response: Response<Q>) {
        return when (response) {
            is ProductResponse -> updateStagingWithProductResponse(response)
            is SubstanceResponse -> updateStagingWithSubstanceResponse(response)
        }
    }

    private fun updateStagingWithProductResponse(response: ProductResponse<Q>) {
        val address = response.address
        when(address.connectionIndex) {
            Heap.VIRTUAL_ADDRESS -> start.find(address.portIndex)
                ?.let { port ->
                    val process = response.value
                    val selectedPortIndex = response.selectedPortIndex
                    val product = process.products[selectedPortIndex].product
                    knowledge[port] = product
                }
            else -> {
                val process = response.value
                val selectedPortIndex = response.selectedPortIndex
                val product = process.products[selectedPortIndex].product
                staging.find(address.connectionIndex)
                    ?.let { existingConnection ->
                        if (existingConnection is EProcess<Q>) {
                            knowledge[existingConnection.inputs[selectedPortIndex].product] = product
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

    private fun updateStagingWithSubstanceResponse(response: SubstanceResponse<Q>) {
        val address = response.address
        when(address.connectionIndex) {
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
