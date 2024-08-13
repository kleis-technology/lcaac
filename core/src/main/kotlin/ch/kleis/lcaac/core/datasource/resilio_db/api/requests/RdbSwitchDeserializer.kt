package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class RdbSwitchDeserializer<Q>(
    private val primaryKey: String,
    ops: QuantityOperations<Q>,
    private val eval: (DataExpression<Q>) -> DataValue<Q>,
    private val pojoDeserializer: PojoDeserializer<Q> = PojoDeserializer(
        ops = ops,
        eval = eval,
    )
) {
    private val keyCpuName = "cpu_name"
    private val keyCpuQuantity = "cpu_quantity"
    private val keyRamTotalSizeGb = "ram_total_size_gb"

    fun schema(): Map<String, DataValue<Q>> = mapOf(
        primaryKey to StringValue("server-01"),
        keyCpuName to StringValue("cpu name"),
        keyCpuQuantity to eval(EDataRef("u")),
        keyRamTotalSizeGb to eval(EDataRef("GB")),
    )

    fun deserialize(record: ERecord<Q>): RdbSwitch {
        val entries = record.entries
        val requiredKeys = setOf(
            primaryKey,
            keyCpuName, keyCpuQuantity,
            keyRamTotalSizeGb,
        )
        val missingKeys = requiredKeys.minus(entries.keys)
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("${{}.javaClass.name}: invalid record: missing keys $missingKeys")
        }
        return RdbSwitch(
            id = pojoDeserializer.readString(primaryKey, entries),
            cpuName = pojoDeserializer.readString(keyCpuName, entries),
            cpuQuantity = pojoDeserializer.readIntUnit(keyCpuQuantity, entries),
            ramTotalSizeGb = pojoDeserializer.readDoubleGb(keyRamTotalSizeGb, entries),
        )
    }
}
