package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue

class RdbRackServerDeserializer<Q>(
    private val primaryKey: String,
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
        keyModelName to StringValue(""),
        keyRackUnit to eval(EDataRef("u")),
        keyCpuName to StringValue("cpu"),
        keyCpuQuantity to eval(EDataRef("u")),
        keyRamTotalSizeGb to eval(EDataRef("GB")),
        keySsdTotalSizeGb to eval(EDataRef("GB")),
    )

    fun deserialize(record: ERecord<Q>): RdbRackServer {
        TODO()
    }
}
