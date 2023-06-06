package ch.kleis.lcaplugin.imports.ecospold.lcia.model

data class UnitConversion(
    val factor: Double,
    val fromUnit: String,
    val toUnit: String,
    val dimension: String,
    val comment: String? = null
)