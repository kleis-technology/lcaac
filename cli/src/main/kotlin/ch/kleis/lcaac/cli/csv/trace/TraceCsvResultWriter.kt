package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicOperations.toDouble
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class TraceCsvResultWriter {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()

    fun header(first: TraceCsvResult): String {
        val header = first.request.columns()
            .plus(listOf("depth", "d_amount", "d_unit", "d_product", "alloc", "name", "a", "b", "c", "amount", "unit"))
            .plus(
                first.trace.first().impacts.toList()
                    .flatMap {
                        listOf(
                            it.first.getShortName(),
                            "${it.first.getShortName()}_unit",
                        )
                    })
        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(header)
        return s.toString()
    }

    @Suppress("DuplicatedCode")
    fun rows(result: TraceCsvResult): List<String> {
        val lines = result.trace.map { line ->
            val demandedAmount = line.demandedProduct.quantity.amount
            val demandedUnit = line.demandedProduct.quantity.unit
            val demandedProductName = line.demandedProduct.product.name
            val allocationAmount = (line.demandedProduct.allocation?.amount?.toDouble()
                ?: 1.0) * (line.demandedProduct.allocation?.unit?.scale ?: 1.0)
            val supplyAmount = line.supply.amount.value * allocationAmount
            val prefix = result.request.arguments().plus(
                when (line.output) {
                    is IndicatorValue<*> -> listOf(
                        line.depth,
                        demandedAmount.toString(),
                        demandedUnit.toString(),
                        demandedProductName,
                        allocationAmount.toString(),
                        line.output.name,
                        "",
                        "",
                        "",
                        supplyAmount.toString(),
                        line.supply.unit.toString(),
                    )

                    is ProductValue<*> ->
                        listOf(
                            line.depth,
                            demandedAmount.toString(),
                            demandedUnit.toString(),
                            demandedProductName,
                            allocationAmount.toString(),
                            line.output.name,
                            line.output.fromProcessRef?.name ?: "",
                            line.output.fromProcessRef?.matchLabels?.toString() ?: "",
                            line.output.fromProcessRef?.arguments?.toString() ?: "",
                            supplyAmount.toString(),
                            line.supply.unit.toString(),
                        )

                    is FullyQualifiedSubstanceValue<*> -> listOf(
                        line.depth,
                        demandedAmount.toString(),
                        demandedUnit.toString(),
                        demandedProductName,
                        allocationAmount.toString(),
                        line.output.name,
                        line.output.compartment,
                        line.output.subcompartment ?: "",
                        line.output.type.toString(),
                        supplyAmount.toString(),
                        line.supply.unit.toString(),
                    )

                    is PartiallyQualifiedSubstanceValue<*> -> listOf(
                        line.depth,
                        demandedAmount.toString(),
                        demandedUnit.toString(),
                        demandedProductName,
                        allocationAmount.toString(),
                        line.output.name,
                        "",
                        "",
                        "",
                        supplyAmount.toString(),
                        line.supply.unit.toString(),
                    )
                }
            )
            val impacts = line.impacts.flatMap {
                val impact = it.value
                val impactAmount = impact.amount.value * allocationAmount
                listOf(
                    impactAmount.toString(),
                    impact.unit.toString(),
                )
            }
            val s = StringBuilder()
            CSVPrinter(s, format).printRecord(prefix.plus(impacts))
            s.toString()
        }
        return lines
    }
}
