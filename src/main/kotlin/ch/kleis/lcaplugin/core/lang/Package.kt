package ch.kleis.lcaplugin.core.lang

class Package(
    val name: String,
    val imports: List<Import>,
    val definitions: Map<String, Expression>
) {
    fun findByLocalName(name: String): Expression? {
        return definitions[name]
    }
}

data class Import(val pkgName: String, val name: String)
