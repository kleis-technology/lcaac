package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.lang.expression.ERecord

data class InMemoryDatasource<Q>(
    val records: List<ERecord<Q>>,
)
