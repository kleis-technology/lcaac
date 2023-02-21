package ch.kleis.lcaplugin.core.lang

class Package(
    val name: String,
    val imports: List<Import>,
    val definitions: Map<String, Expression>
)

sealed interface Import {
    val pkgName: String
}

data class ImportSymbol(override val pkgName: String, val name: String) : Import
data class ImportWildCard(override val pkgName: String) : Import
