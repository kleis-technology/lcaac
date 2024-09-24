package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class RdbRackServerDeserializer<Q>(
    private val primaryKey: String,
    ops: QuantityOperations<Q>,
    private val eval: (DataExpression<Q>) -> DataValue<Q>,
    private val pojoDeserializer: PojoDeserializer<Q> = PojoDeserializer(
        ops = ops,
        eval = eval,
    )
) {
    private val keyModelName = "model_name"
    private val keyRackUnit = "rack_unit"
    private val keyCpuName = "cpu_name"
    private val keyCpuQuantity = "cpu_quantity"
    private val keyRamTotalSizeGb = "ram_total_size_gb"
    private val keySsdTotalSizeGb = "ssd_total_size_gb"
    private val keyGeography = "geography"
    private val keyPowerWatt = "power_watt"
    private val keyDurationOfUseHour = "duration_of_use_hour"

    fun schema(): Map<String, DataValue<Q>> = mapOf(
        primaryKey to StringValue("server-01"),
        keyModelName to StringValue("model name"),
        keyRackUnit to eval(EDataRef("u")),
        keyCpuName to StringValue("cpu name"),
        keyCpuQuantity to eval(EDataRef("u")),
        keyRamTotalSizeGb to eval(EDataRef("GB")),
        keySsdTotalSizeGb to eval(EDataRef("GB")),
        keyGeography to StringValue("global"),
        keyPowerWatt to eval(EDataRef("W")),
        keyDurationOfUseHour to eval(EDataRef("hour")),
    )

    fun deserialize(record: ERecord<Q>): RdbRackServer {
        val entries = record.entries
        val requiredKeys = setOf(
            primaryKey, keyModelName, keyRackUnit,
            keyCpuName, keyCpuQuantity,
            keyRamTotalSizeGb, keySsdTotalSizeGb,
            keyGeography, keyPowerWatt, keyDurationOfUseHour,
        )
        val missingKeys = requiredKeys.minus(entries.keys)
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("${{}.javaClass.name}: invalid record: missing keys $missingKeys")
        }
        return RdbRackServer(
            id = pojoDeserializer.readString(primaryKey, entries),
            modelName = pojoDeserializer.readString(keyModelName, entries),
            rackUnit = pojoDeserializer.readIntUnit(keyRackUnit, entries),
            cpuName = pojoDeserializer.readString(keyCpuName, entries),
            cpuQuantity = pojoDeserializer.readIntUnit(keyCpuQuantity, entries),
            ramTotalSizeGb = pojoDeserializer.readDoubleGb(keyRamTotalSizeGb, entries),
            ssdTotalSizeGb = pojoDeserializer.readDoubleGb(keySsdTotalSizeGb, entries),
            usage = RdbUsage(
                geography = pojoDeserializer.readString(keyGeography, entries),
                powerWatt = pojoDeserializer.readDoubleWatt(keyPowerWatt, entries),
                durationOfUseHour = pojoDeserializer.readDoubleHour(keyDurationOfUseHour, entries),
            ),
        )
    }
}
