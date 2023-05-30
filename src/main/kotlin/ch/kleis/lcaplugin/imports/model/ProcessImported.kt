package ch.kleis.lcaplugin.imports.model

class ProcessImported(
    val uid: String
) {
    var meta: MutableMap<String, String?> = mutableMapOf()
    var params: MutableList<ParamImported> = mutableListOf()
    var comments: MutableList<String> = mutableListOf()

    var productBlocks: MutableList<ExchangeBlock<ProductImported>> = mutableListOf()
    var inputBlocks: MutableList<ExchangeBlock<InputImported>> = mutableListOf()
    var emissionBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()
    var resourceBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()
    var landUseBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()

}

data class ParamImported(val symbol: String, val value: String)

class ExchangeBlock<T : ExchangeImported>(
    val comment: String,
    var exchanges: MutableList<T> = mutableListOf()
)

sealed class ExchangeImported(val comments: List<String>)
class BioExchangeImported(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String,
    val compartment: String,
    var subCompartment: String? = null
) : ExchangeImported(comments)

class ProductImported(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String,
    val allocation: Double = 100.0
) : ExchangeImported(comments) {
    fun asInput(): InputImported {
        return InputImported(comments, qty, unit, uid)
    }
}

class InputImported(
    comments: List<String>,
    val qty: String,
    val unit: String,
    val uid: String
) : ExchangeImported(comments)