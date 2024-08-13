package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.ERecord

data class RdbResponse<Q>(
    val id: String,
    val manufacturing: ERecord<Q>,
    val transport: ERecord<Q>,
    val use: ERecord<Q>,
    val endOfLife: ERecord<Q>,
)

