package ch.kleis.lcaplugin.imports.model

data class ImportedUnit(
    val dimension: String,
    val name: String,
    val scaleFactor: Double,
    val refUnitName: String,
    val comment: String? = null
)