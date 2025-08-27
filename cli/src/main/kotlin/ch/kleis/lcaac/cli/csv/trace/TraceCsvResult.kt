package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class TraceCsvResult(
    val request: CsvRequest,
    val trace: List<TraceCsvResultItem>,
)

data class TraceCsvResultItem(
    val depth: Int,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
)
