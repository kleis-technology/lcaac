package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.SourcePath

/**
 * A step of the evaluation in which an error occurred, e.g. the variable being evaluated.
 * Errors list their frames from the innermost to the outermost.
 */
sealed interface EvaluationFrame {
    val path: SourcePath
    val description: String
}

data class ProcessFrame(override val path: SourcePath.Process) : EvaluationFrame {
    override val description: String get() = describe(path.process)
}

/** [kind] is e.g. "input" or "emission", [name] the product, substance or indicator. */
data class ExchangeFrame(
    override val path: SourcePath.Exchange,
    val kind: String,
    val name: String,
) : EvaluationFrame {
    override val description: String get() = "$kind '$name'"
}

/** [row] is the index of the data source row being evaluated, or null while reading the data source. */
data class ForEachFrame(
    override val path: SourcePath.Exchange,
    val rowRef: String,
    val row: Int? = null,
) : EvaluationFrame {
    override val description: String
        get() = row?.let { "row #${it + 1} of for_each '$rowRef'" } ?: "for_each '$rowRef'"
}

/** A parameter, variable or global being evaluated. */
data class VariableFrame(override val path: SourcePath) : EvaluationFrame {
    override val description: String
        get() = when (path) {
            is SourcePath.Parameter -> "parameter '${path.name}'"
            is SourcePath.Variable -> "variable '${path.name}'"
            is SourcePath.Global -> "global '${path.name}'"
            else -> path.toString()
        }
}

/**
 * The search for the supplier of [product], required by [process]. [path] is the input
 * that requires it, or the process when the input is not known (e.g. a cached process).
 */
data class SupplierFrame(
    override val path: SourcePath,
    val product: String,
    val process: ProcessKey,
) : EvaluationFrame {
    override val description: String get() = "supplier of '$product' for ${describe(process)}"
}

data class SubstanceFrame(override val path: SourcePath.Substance) : EvaluationFrame {
    override val description: String get() = "substance '${path.substance.name}'"
}

/**
 * The search for the characterization of [substance], emitted or consumed by [process].
 * [path] is the exchange that requires it, or the process when the exchange is not known.
 */
data class CharacterizationFrame(
    override val path: SourcePath,
    val substance: String,
    val process: ProcessKey,
) : EvaluationFrame {
    override val description: String get() = "characterization of '$substance' for ${describe(process)}"
}

data class ReferenceUnitFrame(override val path: SourcePath.ReferenceUnit) : EvaluationFrame {
    override val description: String get() = "reference unit of substance '${path.substance.name}'"
}

/** The product [product] of [process], e.g. as the definition that an input refers to. */
data class ProductFrame(
    override val path: SourcePath,
    val product: String,
    val process: ProcessKey,
) : EvaluationFrame {
    override val description: String get() = "product '$product' of ${describe(process)}"
}

private fun describe(process: ProcessKey): String = "process '$process'"

/** The kind of a biosphere exchange, e.g. "emission". */
internal fun <Q> kindOf(substance: ESubstanceSpec<Q>): String =
    substance.type?.value?.lowercase() ?: "biosphere exchange"

/**
 * Runs [block], adding the frame given by [frame] to any [EvaluatorException] it throws.
 * The frame is only built when there is an error.
 */
internal inline fun <T> within(frame: () -> EvaluationFrame?, block: () -> T): T =
    try {
        block()
    } catch (e: EvaluatorException) {
        frame()?.let { e.within(it) }
        throw e
    }
