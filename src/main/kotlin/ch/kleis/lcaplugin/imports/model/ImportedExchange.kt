package ch.kleis.lcaplugin.imports.model

sealed class ImportedExchange(val comments: List<String>)
class ImportedBioExchange(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String,
    val compartment: String,
    var subCompartment: String? = null
) : ImportedExchange(comments)

class ImportedInputExchange(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String
) : ImportedExchange(comments)

class ImportedProductExchange(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String,
    val allocation: Double = 100.0
) : ImportedExchange(comments) {
    fun asInput(): ImportedInputExchange {
        return ImportedInputExchange(comments, qty, unit, uid)
    }
}