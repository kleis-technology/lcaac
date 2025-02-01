package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.math.QuantityOperations
import kotlinx.serialization.json.*

class RdbResponseDeserializer<Q>(
    private val primaryKey: String,
    private val lcStepMapping: LcStepMapping,
    private val ops: QuantityOperations<Q>,
) {
    fun decodeFromString(
        responseKey: String,
        id: String,
        body: String,
    ): RdbResponse<Q> {
        val root = Json.decodeFromString<JsonObject>(body)
        val results = root["results"] as JsonObject
        val impacts = results[responseKey] as JsonObject
        val perLcStep = impacts["per_lc_step"] as JsonObject
        return RdbResponse(
            id = id,
            manufacturing = decodeLcStepFromString(
                id,
                ops,
                RdbLcStep.MANUFACTURING,
                perLcStep,
            ),
            transport = decodeLcStepFromString(
                id,
                ops,
                RdbLcStep.TRANSPORT,
                perLcStep,
            ),
            use = decodeLcStepFromString(
                id,
                ops,
                RdbLcStep.USE,
                perLcStep,
            ),
            endOfLife = decodeLcStepFromString(
                id,
                ops,
                RdbLcStep.END_OF_LIFE,
                perLcStep,
            ),
        )
    }

    private fun decodeLcStepFromString(
        id: String,
        ops: QuantityOperations<Q>,
        lcStep: RdbLcStep,
        perLcStep: JsonObject,
    ): ERecord<Q> {
        val rawEntries = perLcStep[lcStep.rdbField] as JsonObject
        val impactEntries = RdbIndicator.entries.associate {
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
        val entries = impactEntries
            .plus(primaryKey to EStringLiteral(id))
            .plus(lcStepMapping.key to EStringLiteral(lcStepMapping.getMappedName(lcStep)))
        return ERecord(entries)
    }
}
