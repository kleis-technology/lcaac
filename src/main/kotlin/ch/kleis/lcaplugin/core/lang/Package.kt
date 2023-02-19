package ch.kleis.lcaplugin.core.lang

data class Definition(val name: String, val expression: Expression)
data class Package(
    val name: String,
    val environment: Map<String, Expression>,
) {
    constructor(name: String, definitions: List<Definition>): this(name, definitions.associate { Pair(it.name, it.expression) })

    fun get(name: String): Expression? {
        return environment[name]
    }
}
