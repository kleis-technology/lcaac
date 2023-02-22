package ch.kleis.lcaplugin.core.lang

class Package(
    val name: String,
    val imports: List<Import>,
    val definitions: Environment
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Package

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

sealed interface Import {
    val pkgName: String
}

data class ImportSymbol(override val pkgName: String, val name: String) : Import
data class ImportWildCard(override val pkgName: String) : Import
