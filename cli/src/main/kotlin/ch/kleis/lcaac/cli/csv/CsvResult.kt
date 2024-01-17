package ch.kleis.lcaac.cli.csv

import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class CsvResult(
    val request: CsvRequest,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
)
