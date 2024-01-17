package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

class BasicTestRunner<S>(
    private val symbolTable: SymbolTable<BasicNumber>
) {
    private val evaluator = Evaluator(symbolTable, BasicOperations)
    private val dataReducer = DataExpressionReducer(symbolTable.data, BasicOperations)

    fun run(case: TestCase<S>): TestResult<S> {
        val trace = evaluator.with(case.template).trace(case.template)
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()
        val target = trace.getEntryPoint().products.first().port()
        val results = case.assertions.map { assertion ->
            val ports = analysis.findAllPortsByShortName(assertion.ref)
            val impact = with(QuantityValueOperations(BasicOperations)) {
                ports.map {
                    if (analysis.isControllable(it)) analysis.getPortContribution(target, it)
                    else analysis.supplyOf(it)
                }.reduce { acc, quantityValue -> acc + quantityValue }
            }
            val lo = with(ToValue(BasicOperations)) { dataReducer.reduce(assertion.lo).toValue() }
            val hi = with(ToValue(BasicOperations)) { dataReducer.reduce(assertion.hi).toValue() }
            test(assertion.ref, impact, lo, hi)
        }
        return TestResult(
            case.source,
            case.name,
            results,
        )
    }

    private fun test(name: String, impact: QuantityValue<BasicNumber>, lo: DataValue<BasicNumber>, hi: DataValue<BasicNumber>): AssertionResult {
        with(QuantityValueOperations(BasicOperations)) {
            val actual = impact.toDouble()
            return when {
                lo is QuantityValue<BasicNumber> && hi is QuantityValue<BasicNumber> ->
                    when {
                        !allTheSameDimension(impact, lo, hi) ->
                            GenericFailure("incompatible dimensions: $name (${impact.unit.dimension}) between $lo (${lo.unit.dimension}) and $hi (${hi.unit.dimension})")
                        lo.toDouble() <= actual && actual <= hi.toDouble() ->
                            RangeAssertionSuccess(name, lo, hi, impact)
                        else -> RangeAssertionFailure(name, lo, hi, impact)
                    }

                else -> GenericFailure("invalid range: $lo and $hi")
            }
        }
    }

    private fun allTheSameDimension(a: QuantityValue<BasicNumber>, b: QuantityValue<BasicNumber>, c: QuantityValue<BasicNumber>): Boolean {
        return a.unit.dimension == b.unit.dimension
            && b.unit.dimension == c.unit.dimension
    }
}
