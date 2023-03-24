package ch.kleis.lcaplugin.core.lang.evaluator.linker

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.evaluator.compiler.UnlinkedSystem
import ch.kleis.lcaplugin.core.lang.value.*

class Linker {
    private val everyInputProduct =
        ProcessValue.inputs compose
                Every.list() compose TechnoExchangeValue.product

    fun link(
        systemObject: UnlinkedSystem
    ): SystemValue {
        val productMatcher = ProductMatcher(systemObject)
        val processes = systemObject.getProcesses()
            .map { process ->
                everyInputProduct.modify(process) {
                    productMatcher.match(it) ?: it
                }
            }
        val substanceCharacterizations = systemObject.getSubstanceCharacterizations().toList()
        return SystemValue(
            processes,
            substanceCharacterizations,
        )
    }
}

