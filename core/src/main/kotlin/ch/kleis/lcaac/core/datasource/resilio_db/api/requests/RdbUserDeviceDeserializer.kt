package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class RdbUserDeviceDeserializer<Q>(
    private val primaryKey: String,
    ops: QuantityOperations<Q>,
    private val eval: (DataExpression<Q>) -> DataValue<Q>,
    private val pojoDeserializer: PojoDeserializer<Q> = PojoDeserializer(
        ops = ops,
        eval = eval,
    )
) {
    private val keyDeviceType = "device_type"
    private val keyModelName = "model_name"
    private val keyGeography = "geography"
    private val keyPowerWatt = "power_watt"
    private val keyDurationOfUseHour = "duration_of_use_hour"

    fun schema(): Map<String, DataValue<Q>> = mapOf(
        primaryKey to StringValue("server-01"),
        keyModelName to StringValue("model name"),
        keyDeviceType to StringValue("device type"),
        keyGeography to StringValue("global"),
        keyPowerWatt to eval(EDataRef("W")),
        keyDurationOfUseHour to eval(EDataRef("hour")),
    )

    fun deserialize(record: ERecord<Q>): RdbUserDevice {
        val entries = record.entries
        val requiredKeys = setOf(
            primaryKey, keyModelName, keyDeviceType,
            keyGeography, keyPowerWatt, keyDurationOfUseHour,
        )
        val missingKeys = requiredKeys.minus(entries.keys)
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("${{}.javaClass.name}: invalid record: missing keys $missingKeys")
        }
        return RdbUserDevice(
            id = pojoDeserializer.readString(primaryKey, entries),
            deviceType = RdbUserDeviceType.from(pojoDeserializer.readString(keyDeviceType, entries)),
            modelName = pojoDeserializer.readString(keyModelName, entries),
            usage = RdbUsage(
                geography = pojoDeserializer.readString(keyGeography, entries),
                powerWatt = pojoDeserializer.readDoubleWatt(keyPowerWatt, entries),
                durationOfUseHour = pojoDeserializer.readDoubleHour(keyDurationOfUseHour, entries),
            ),
        )
    }
}
