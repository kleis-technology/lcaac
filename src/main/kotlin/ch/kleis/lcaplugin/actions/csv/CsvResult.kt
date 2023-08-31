package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue

data class CsvResult<Q>(
    val request: CsvRequest,
    val output: MatrixColumnIndex<Q>,
    val impacts: Map<MatrixColumnIndex<Q>, QuantityValue<Q>>,
)
