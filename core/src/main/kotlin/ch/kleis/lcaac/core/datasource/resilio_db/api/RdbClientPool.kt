package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.math.QuantityOperations

class RdbClientPool<Q>(
    private val url: String,
    private val accessToken: String,
    private val version: String,
    private val ops: QuantityOperations<Q>,
) {
    private val clients: HashMap<Pair<String, LcStepMapping>, RdbClient<Q>> = HashMap()

    fun get(primaryKey: String, lcStepMapping: LcStepMapping): RdbClient<Q> {
        val key = primaryKey to lcStepMapping
        if (!clients.containsKey(key)) {
            val client = RdbClient(
                url = url,
                accessToken = accessToken,
                primaryKey = primaryKey,
                version = version,
                lcStepMapping = lcStepMapping,
                ops = ops,
            )
            clients[key] = client
            return client
        }
        return clients[primaryKey to lcStepMapping]!!
    }
}
