package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

data class InMemoryDatasource<Q>(
    val schema: Map<String, DataValue<Q>>,
    val records: List<InMemoryRecord>,
)
typealias InMemoryRecord = Map<String, InMemoryValue>

sealed interface InMemoryValue
data class InMemStr(val value: String) : InMemoryValue
data class InMemNum(val value: Double) : InMemoryValue
