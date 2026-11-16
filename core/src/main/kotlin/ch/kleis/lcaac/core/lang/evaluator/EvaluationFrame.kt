package ch.kleis.lcaac.core.lang.evaluator

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
    override val description: String get() = "process '${path.process}'"
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

/** The search for the supplier of [product], required by the process at [path]. */
data class SupplierFrame(
    override val path: SourcePath.Process,
    val product: String,
) : EvaluationFrame {
    override val description: String get() = "supplier of '$product' for process '${path.process}'"
}

data class SubstanceFrame(override val path: SourcePath.Substance) : EvaluationFrame {
    override val description: String get() = "substance '${path.substance.name}'"
}

/** The search for the characterization of [substance], emitted or consumed by the process at [path]. */
data class CharacterizationFrame(
    override val path: SourcePath.Process,
    val substance: String,
) : EvaluationFrame {
    override val description: String get() = "characterization of '$substance' for process '${path.process}'"
}

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
