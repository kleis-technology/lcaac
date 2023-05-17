package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue

data class CsvResult(
    val request: CsvRequest,
    val output: MatrixColumnIndex,
    val impacts: Map<MatrixColumnIndex, QuantityValue>,
)
