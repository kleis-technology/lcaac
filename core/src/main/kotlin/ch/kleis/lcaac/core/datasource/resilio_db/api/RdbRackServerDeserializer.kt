package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityDiv
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class RdbRackServerDeserializer<Q>(
    private val primaryKey: String,
    private val ops: QuantityOperations<Q>,
    private val eval: (DataExpression<Q>) -> DataValue<Q>,
) {
    private val keyModelName = "model_name"
    private val keyRackUnit = "rack_unit"
    private val keyCpuName = "cpu_name"
    private val keyCpuQuantity = "cpu_quantity"
    private val keyRamTotalSizeGb = "ram_total_size_gb"
    private val keySsdTotalSizeGb = "ssd_total_size_gb"

    fun schema(): Map<String, DataValue<Q>> = mapOf(
        primaryKey to StringValue("server-01"),
        keyModelName to StringValue("model name"),
        keyRackUnit to eval(EDataRef("u")),
        keyCpuName to StringValue("cpu name"),
        keyCpuQuantity to eval(EDataRef("u")),
        keyRamTotalSizeGb to eval(EDataRef("GB")),
        keySsdTotalSizeGb to eval(EDataRef("GB")),
    )

    fun deserialize(record: ERecord<Q>): RdbRackServer {
        val entries = record.entries
        val requiredKeys = setOf(
            primaryKey, keyModelName, keyRackUnit,
            keyCpuName, keyCpuQuantity,
            keyRamTotalSizeGb, keySsdTotalSizeGb,
        )
        val missingKeys = requiredKeys.minus(entries.keys)
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("${{}.javaClass.name}: invalid record: missing keys $missingKeys")
        }
        return RdbRackServer(
            id = readString(primaryKey, entries),
            modelName = readString(keyModelName, entries),
            rackUnit = readIntUnit(keyRackUnit, entries),
            cpuName = readString(keyCpuName, entries),
            cpuQuantity = readIntUnit(keyCpuQuantity, entries),
            ramTotalSizeGb = readDoubleGb(keyRamTotalSizeGb, entries),
            ssdTotalSizeGb = readDoubleGb(keySsdTotalSizeGb, entries),
        )
    }

    private fun readString(key: String, entries: Map<String, DataExpression<Q>>): String {
        val data = entries[key]!!
        return when (val value = eval(data)) {
            is StringValue -> value.s
            else -> throw IllegalArgumentException("${{}.javaClass.name}: invalid record entry '$key': expecting a string, found '$value'")
        }
    }

    private fun readIntUnit(key: String, entries: Map<String, DataExpression<Q>>): Int {
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

    private fun readDoubleGb(key: String, entries: Map<String, DataExpression<Q>>): Double {
        val data = entries[key]!!
        val unit = EDataRef<Q>("GB")
        val ratio = EQuantityDiv(data, unit)
        return when (val value = eval(ratio)) {
            is QuantityValue -> with(ops) {
                value.amount.toDouble() * value.unit.scale
            }

            else -> throw IllegalArgumentException("${{}.javaClass.name}: invalid record entry '$key': expecting a quantity, found '$value'")
        }
    }


}
