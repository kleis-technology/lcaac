package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.value.*

class EMapper {
    fun <Q> toDataExpression(value: DataValue<Q>): DataExpression<Q> {
        return when (value) {
            is QuantityValue -> value.toEQuantityScale()
            is RecordValue -> value.toERecord()
            is StringValue -> value.toEStringLiteral()
        }
    }

    fun <Q> toFromProcess(value: FromProcessRefValue<Q>): FromProcess<Q> {
        val labels = MatchLabels(value.matchLabels.map { it.key to it.value.toEStringLiteral() }.toMap())
        val arguments: Map<String, DataExpression<Q>> = value.arguments.map { it.key to toDataExpression(it.value) }.toMap()
        return FromProcess(value.name, labels, arguments)
    }

    fun <Q> toETechnoExchange(quantity: QuantityValue<Q>, product: ProductValue<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            quantity = quantity.toEQuantityScale(),
            product = EProductSpec(
                product.name,
                product.referenceUnit.toEUnitLiteral(),
                product.fromProcessRef?.let { toFromProcess(it) }
            )
        )
    }
}