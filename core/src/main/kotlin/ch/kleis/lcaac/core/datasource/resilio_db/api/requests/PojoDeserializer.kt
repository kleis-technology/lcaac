package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityDiv
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class PojoDeserializer<Q>(
    private val ops: QuantityOperations<Q>,
    private val eval: (DataExpression<Q>) -> DataValue<Q>,
) {
    fun readString(key: String, entries: Map<String, DataExpression<Q>>): String {
        val data = entries[key]!!
        return when (val value = eval(data)) {
            is StringValue -> value.s
            else -> throw IllegalArgumentException("${{}.javaClass.name}: invalid record entry '$key': expecting a string, found '$value'")
        }
    }

    fun readIntUnit(key: String, entries: Map<String, DataExpression<Q>>): Int {
        val data = entries[key]!!
        val unit = EDataRef<Q>("u")
        val ratio = EQuantityDiv(data, unit)
        return when (val value = eval(ratio)) {
            is QuantityValue -> with(ops) {
                (value.amount.toDouble() * value.unit.scale).toInt()
            }

            else -> throw IllegalArgumentException("${{}.javaClass.name}: invalid record entry '$key': expecting a quantity, found '$value'")
        }
    }

    private fun readDouble(key: String, entries: Map<String, DataExpression<Q>>, unitRef: String): Double {
        val data = entries[key]!!
        val unit = EDataRef<Q>(unitRef)
        val ratio = EQuantityDiv(data, unit)
        return when (val value = eval(ratio)) {
            is QuantityValue -> with(ops) {
                value.amount.toDouble() * value.unit.scale
            }

            else -> throw IllegalArgumentException("${{}.javaClass.name}: invalid record entry '$key': expecting a quantity, found '$value'")
        }

    }

    fun readDoubleGb(key: String, entries: Map<String, DataExpression<Q>>): Double = readDouble(key, entries, "GB")
    fun readDoubleWatt(key: String, entries: Map<String, DataExpression<Q>>): Double = readDouble(key, entries, "W")
    fun readDoubleHour(key: String, entries: Map<String, DataExpression<Q>>): Double = readDouble(key, entries, "hour")

}
