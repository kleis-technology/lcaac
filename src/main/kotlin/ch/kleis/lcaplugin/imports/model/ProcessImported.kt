package ch.kleis.lcaplugin.imports.model

class ProcessImported(
    val uid: String,
    val name: String,
) {
    var meta: MutableMap<String, String?> = mutableMapOf("name" to name)

    var productBlocks: MutableList<ExchangeBlock<ProductImported>> = mutableListOf()
    var inputBlocks: MutableList<ExchangeBlock<InputImported>> = mutableListOf()
    var emissionBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()
    var resourceBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()
    var landUseBlocks: MutableList<ExchangeBlock<BioExchangeImported>> = mutableListOf()

}

class ExchangeBlock<T : ExchangeImported>(
    val comment: String,
    var exchanges: MutableList<T> = mutableListOf()
)

sealed class ExchangeImported(val comments: List<String>)
class BioExchangeImported(
    comments: List<String>,
    val qty: Double,
    val unit: String,
    val uid: String,
    val compartment: String,
    var subCompartment: String? = null
) : ExchangeImported(comments)

class ProductImported(
    comments: List<String>,
    val qty: Double,
    val unit: String,
    val uid: String,
    val allocation: Double = 100.0
) : ExchangeImported(comments)

class InputImported(
    comments: List<String>,
    val qty: Double,
    val unit: String,
    val uid: String
) : ExchangeImported(comments)