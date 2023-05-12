package ch.kleis.lcaplugin.imports.model

data class UnitImported(
    val dimension: String,
    val name: String,
    val scaleFactor: Double,
    val refUnitName: String
)