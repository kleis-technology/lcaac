package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator

data class Program(
    private val environment: Environment,
    private val entryPoint: Expression,
) {
    fun run(): Value {
        return Evaluator(environment).eval(entryPoint)
    }
}
