package ch.kleis.lcaplugin.imports.ecospold.model

data class Uncertainty(
    val logNormal: LogNormal? = null,
    val normal: Normal? = null,
    val triangular: Triangular? = null,
    val uniform: Uniform? = null,
    val undefined: UndefinedUncertainty? = null,
    val pedigreeMatrix: PedigreeMatrix? = null,
    val comment: String? = null
)

data class LogNormal(
    val meanValue: Double,
    val mu: Double,
    val variance: Double,
    val varianceWithPedigreeUncertainty: Double
)

data class Normal(
    val meanValue: Double,
    val variance: Double,
    val varianceWithPedigreeUncertainty: Double
)

data class PedigreeMatrix(
    val reliability: Int,
    val completeness: Int,
    val temporalCorrelation: Int,
    val geographicalCorrelation: Int,
    val furtherTechnologyCorrelation: Int
)

data class Triangular(
    val minValue: Double,
    val mostLikelyValue: Double,
    val maxValue: Double
)

data class UndefinedUncertainty(
    val minValue: Double,
    val maxValue: Double,
    val standardDeviation95: Double
)

data class Uniform(
    val minValue: Double,
    val maxValue: Double
)