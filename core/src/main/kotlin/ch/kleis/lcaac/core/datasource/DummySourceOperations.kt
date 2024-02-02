package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.*

/*
    The dummy source ops does not read from any file.
    It simply returns the default record defined by the source's schema.
    It is used when reducing a EUnitOf, as there is no need to fetch
    an actual value from the data source, but only learn about the relevant dimension.
 */

class DummySourceOperations<Q> : DataSourceOperations<Q> {
    override fun readAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        return sequenceOf(ERecord(source.schema.mapValues { it.value.toDataExpression() }))
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        return source.schema.filterKeys { columns.contains(it) }
            .map { it.value.toDataExpression() }
            .reduce { acc, dataExpression -> EQuantityMul(acc, dataExpression) }
    }

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        return ERecord(source.schema.mapValues { it.value.toDataExpression() })
    }

    private fun DataValue<Q>.toDataExpression(): DataExpression<Q> {
        return when (this) {
            is QuantityValue -> this.toEQuantityScale()
            is RecordValue -> ERecord(this.entries.mapValues { it.value.toDataExpression() })
            is StringValue -> EStringLiteral(this.s)
        }
    }
}
