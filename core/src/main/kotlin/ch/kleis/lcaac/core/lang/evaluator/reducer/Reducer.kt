package ch.kleis.lcaac.core.lang.evaluator.reducer

interface Reducer<E> {
    fun reduce(expression: E): E
}
