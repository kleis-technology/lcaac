package ch.kleis.lcaplugin.core.lang

data class Environment(
    private val data: HashMap<String, Expression> = HashMap()
) {
    constructor(environment: Environment): this(environment.data)

    companion object {
        fun <E : Expression> of(vararg  pairs: Pair<String, E>): Environment {
            val env = Environment()
            pairs.forEach {
                env[it.first] = it.second
            }
            return env
        }

        fun of(map: Map<String, Expression>): Environment {
            val env = Environment()
            map.forEach {
                env[it.key] = it.value
            }
            return env
        }
    }

    operator fun get(key: String): Expression? {
        return data[key]
    }

    operator fun set(key: String, expression: Expression) {
        if (data.containsKey(key) && data[key] != expression) {
            throw IllegalArgumentException("cannot set $key = $expression : variable $key already defined with value ${data[key]}")
        }
        data[key] = expression
    }

    fun forEach(action: (String, Expression) -> Unit): Unit {
        data.forEach(action)
    }

    fun keys(): Collection<String> {
        return data.keys
    }
}

fun emptyEnv(): Environment {
    return Environment()
}
