package ch.kleis.lcaac.cli.csv.assess

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class AssessCsvResult(
    val request: CsvRequest,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
)
