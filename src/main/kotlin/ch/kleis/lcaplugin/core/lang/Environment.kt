package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

class Environment<E>(
    data: HashMap<String, E> = HashMap()
) {
    private val data = HashMap(data)

    constructor(environment: Environment<E>) : this(environment.data)

    companion object {
        fun <E> empty(): Environment<E> {
            return Environment()
        }
    }

    operator fun get(key: String): E? {
        return data[key]
    }

    operator fun set(key: String, e: E) {
        if (data.containsKey(key)) {
            throw EvaluatorException("reference $key already bound: $key = ${data[key]}")
        }
        data[key] = e
    }
}
