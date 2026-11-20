package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.assessment.TraceRow

typealias TraceCsvResultItem = TraceRow

data class TraceCsvResult(
    val request: CsvRequest,
    val trace: List<TraceCsvResultItem>,
)
