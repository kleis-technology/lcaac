package ch.kleis.lcaplugin.core.lang

class Package(
    private val definitions: Map<String, Expression>
) {
    fun get(name: String): Expression? {
        return definitions[name]
    }

    fun getDefinitions(): Map<String, Expression> {
        return definitions
    }
}
