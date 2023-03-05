package ch.kleis.lcaplugin.core.lang.evaluator

interface Reducer<E> {
    fun reduce(expression: E): E
}
