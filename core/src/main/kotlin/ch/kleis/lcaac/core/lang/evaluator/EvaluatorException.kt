package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.source.SourceMap

class EvaluatorException(message: String) : Exception(message) {
    private val frameList = ArrayList<EvaluationFrame>()

    /** Where the error occurred, from the innermost to the outermost step. */
    val frames: List<EvaluationFrame> get() = frameList

    /** Locations of the frames, set by the evaluator. */
    var sourceMap: SourceMap = SourceMap.empty()
        private set

    /** Adds an enclosing frame, unless it is already the outermost one (e.g. a for_each and one of its rows). */
    fun within(frame: EvaluationFrame): EvaluatorException {
        if (frameList.lastOrNull()?.path != frame.path) frameList.add(frame)
        return this
    }

    fun withSourceMap(sourceMap: SourceMap): EvaluatorException {
        this.sourceMap = sourceMap
        return this
    }

    /** The message followed by one line per frame, with its location when known. */
    fun describe(): String = buildString {
        append(message)
        frames.forEach { frame ->
            append("\n  in ${frame.description}")
            sourceMap[frame.path]?.let { append(" ($it)") }
        }
    }
}
