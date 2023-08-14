package ch.kleis.lcaplugin.imports.model

class ImportedProcess(
    val uid: String,
    val meta: Map<String, String?> = emptyMap(),
    val productBlocks: List<ExchangeBlock<ImportedProductExchange>> = emptyList(),
    val inputBlocks: List<ExchangeBlock<ImportedInputExchange>> = emptyList(),
    val emissionBlocks: List<ExchangeBlock<ImportedBioExchange>> = emptyList(),
    val resourceBlocks: List<ExchangeBlock<ImportedBioExchange>> = emptyList(),
    val landUseBlocks: List<ExchangeBlock<ImportedBioExchange>> = emptyList(),
    val impactBlocks: List<ExchangeBlock<ImportedImpactExchange>> = emptyList(),
) {
    var params: MutableList<ImportedParam> = mutableListOf()
    var comments: MutableList<String> = mutableListOf()
}

data class ImportedParam(val symbol: String, val value: String)

class ExchangeBlock<T : ImportedExchange>(
    val comment: String? = null,
    var exchanges: Sequence<T> = emptySequence()
)

