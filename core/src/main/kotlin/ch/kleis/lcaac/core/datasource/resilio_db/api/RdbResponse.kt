package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.QuantityOperations
import kotlinx.serialization.json.*

class RdbResponseDeserializer<Q>(
    private val ops: QuantityOperations<Q>,
) {
    fun decodeFromString(
        id: String,
        body: String,
    ): RdbResponse<Q> {
        val root = Json.decodeFromString<JsonObject>(body)
        val results = root["results"] as JsonObject
        val impacts = results[id] as JsonObject
        val perLcStep = impacts["per_lc_step"] as JsonObject
        return RdbResponse(
            id = id,
            manufacturing = decodeLcStepFromString(
                ops,
                RdbLcStep.MANUFACTURING,
                perLcStep,
            ),
            transport = decodeLcStepFromString(
                ops,
                RdbLcStep.TRANSPORT,
                perLcStep,
            ),
            use = decodeLcStepFromString(
                ops,
                RdbLcStep.USE,
                perLcStep,
            ),
            endOfLife = decodeLcStepFromString(
                ops,
                RdbLcStep.END_OF_LIFE,
                perLcStep,
            ),
        )
    }

    private fun decodeLcStepFromString(
        ops: QuantityOperations<Q>,
        lcStep: RdbLcStep,
        perLcStep: JsonObject,
    ): ERecord<Q> {
        val rawEntries = perLcStep[lcStep.rdbField] as JsonObject
        val entries = RdbIndicator.entries.associate {
            val rawEntry = rawEntries[it.rdbField]
                ?: throw IllegalStateException("missing indicator '${it.rdbField}' in resilio db response")
            when (rawEntry) {
                JsonNull -> it.name to EQuantityScale(
                    ops.pure(0.0),
                    it.getUnit(),
                )

                is JsonPrimitive -> {
                    val amount = Json.decodeFromJsonElement<Double>(rawEntry)
                    it.name to EQuantityScale(
                        ops.pure(amount),
                        it.getUnit(),
                    )
                }

                else -> throw IllegalArgumentException("invalid resilio db response: expecting 'null' or number, found $rawEntry")
            }
        }
        return ERecord(entries)
    }
}

data class RdbResponse<Q>(
    val id: String,
    val manufacturing: ERecord<Q>,
    val transport: ERecord<Q>,
    val use: ERecord<Q>,
    val endOfLife: ERecord<Q>,
)

