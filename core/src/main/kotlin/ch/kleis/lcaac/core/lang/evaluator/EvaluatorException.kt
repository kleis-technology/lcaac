package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.source.SourceMap

class EvaluatorException(message: String) : Exception(message) {
    private val frameList = ArrayList<EvaluationFrame>()

    /** Where the error occurred, from the innermost to the outermost step. */
    val frames: List<EvaluationFrame> get() = frameList

    private val relatedList = ArrayList<EvaluationFrame>()

    /** Other definitions involved in the error, e.g. a reference unit that a quantity does not match. */
    val related: List<EvaluationFrame> get() = relatedList

    /** Locations of the frames, set by the evaluator. */
    var sourceMap: SourceMap = SourceMap.empty()
        private set

    /** Adds an enclosing frame, unless it is already the outermost one (e.g. a for_each and one of its rows). */
    fun within(frame: EvaluationFrame): EvaluatorException {
        if (frameList.lastOrNull()?.path != frame.path) frameList.add(frame)
        return this
    }

    fun seeAlso(frame: EvaluationFrame): EvaluatorException {
        relatedList.add(frame)
        return this
    }

    fun withSourceMap(sourceMap: SourceMap): EvaluatorException {
        this.sourceMap = sourceMap
        return this
    }

    /** The message followed by one line per frame and per related definition, with its location when known. */
    fun describe(): String = buildString {
        append(message)
        frames.forEach { line("in", it) }
        related.forEach { line("see", it) }
    }

    private fun StringBuilder.line(prefix: String, frame: EvaluationFrame) {
        append("\n  $prefix ${frame.describe(sourceMap)}")
        sourceMap[frame.path]?.let { append(" ($it)") }
    }
}
