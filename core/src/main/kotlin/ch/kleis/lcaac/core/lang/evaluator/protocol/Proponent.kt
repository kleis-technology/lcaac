package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class Proponent<Q>(
    private val requests: Set<Request<Q>>,
    private val ops: QuantityOperations<Q>,
) {
    private val incompleteConnections = Heap<ConnectionExpression<Q>>()
    private val alreadyRequestedPorts = HashSet<PortExpression<Q>>()
    val trace: EvaluationTrace<Q> = EvaluationTrace.empty()

    fun start(): Set<Request<Q>> {
        alreadyRequestedPorts.addAll(
            requests.map { it.value }
        )
        return requests
    }

    fun receive(responses: Set<Response<Q>>): Set<Request<Q>> {
        /*
            Receive responses.
         */
        val nextRequests = responses.flatMap { response ->
            when (response) {
                is ProductResponse -> receiveProcessResponse(response)
                is SubstanceResponse -> receiveSubstanceResponse(response)
            }
        }.filter { !alreadyRequestedPorts.contains(it.value) }.toSet()

        /*
            Commit.
         */
        responses
            .map { it.address.connectionIndex }.forEach { connectionIndex ->
                incompleteConnections.pop(connectionIndex) { connection ->
                    with(ToValue(ops)) {
                        when (connection) {
                            is EProcess -> connection.toValue()
                                .let { trace.addIfNew(it) }

                            is ESubstanceCharacterization -> connection.toValue()
                                .let { trace.addIfNew(it) }
                        }
                    }
                }
            }
        trace.commit()

        alreadyRequestedPorts.addAll(
            nextRequests.map { it.value }
        )
        return nextRequests
    }

    fun finish() {
        incompleteConnections.popAll().forEach {
            with(ToValue(ops)) {
                when (it) {
                    is EProcess -> trace.addIfNew(it.toValue())
                    is ESubstanceCharacterization -> trace.addIfNew(it.toValue())
                }
            }
        }
        trace.commit()
    }

    private fun receiveProcessResponse(
        response: ProductResponse<Q>
    ): Set<Request<Q>> {
        val address = response.address
        val process = response.value
        val selectedPortIndex = response.selectedPortIndex

        /*
            Update heap.
         */
        val product = process.products[selectedPortIndex].product
        incompleteConnections.modify(
            address.connectionIndex,
            setProcessInputProduct(address.portIndex, product),
        )

        /*
            Add to heap or return empty if already stored.
         */
        if (incompleteConnections.contains(process)) return emptySet()
        val connectionIndex = incompleteConnections.store(process)

        /*
            Generate next requests.
         */
        val productRequests =
            process.inputs.mapIndexed { portIndex, it ->
                ProductRequest(PAddr(connectionIndex, portIndex), it.product)
            }.toSet()
        val substanceRequest =
            process.biosphere.mapIndexed { portIndex, it ->
                SubstanceRequest(SAddr(connectionIndex, portIndex), it.substance)
            }.toSet()
        return productRequests + substanceRequest
    }

    private fun receiveSubstanceResponse(
        response: SubstanceResponse<Q>
    ): Set<Request<Q>> {
        val address = response.address
        val substanceCharacterization = response.value

        /*
            Update heap.
         */
        val substance = substanceCharacterization.referenceExchange.substance
        incompleteConnections.modify(
            address.connectionIndex,
            setProcessInputSubstance(address.portIndex, substance),
        )

        /*
            Add to heap if not already stored.
         */
        if (incompleteConnections.contains(substanceCharacterization)) return emptySet()
        incompleteConnections.store(substanceCharacterization)

        /*
            A substance characterization generates no further requests.
         */
        return emptySet()
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
