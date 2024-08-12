package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.lang.value.RecordValue

data class InMemoryDatasource<Q>(
    val records: List<RecordValue<Q>>,
)
