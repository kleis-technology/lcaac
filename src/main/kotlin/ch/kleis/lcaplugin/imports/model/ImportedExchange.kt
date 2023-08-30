package ch.kleis.lcaplugin.imports.model

sealed interface ImportedExchange {
    val qty: String
    val unit: String
    val uid: String
    val comments: List<String>
    val printAsComment: Boolean
}

sealed interface ImportedTechnosphereExchange

data class ImportedBioExchange(
    override val qty: String,
    override val unit: String,
    override val uid: String,
    val compartment: String,
    var subCompartment: String? = null,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedExchange

data class ImportedInputExchange(
    override val qty: String,
    override val unit: String,
    override val uid: String,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedTechnosphereExchange, ImportedExchange {
    companion object
}

data class ImportedProductExchange(
    override val qty: String,
    override val unit: String,
    override val uid: String,
    val allocation: Double = 100.0,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedTechnosphereExchange, ImportedExchange {
    companion object

    fun asInput(): ImportedInputExchange = ImportedInputExchange(qty, unit, uid, comments)
}

data class ImportedImpactExchange(
    override val qty: String,
    override val unit: String,
    override val uid: String,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedExchange