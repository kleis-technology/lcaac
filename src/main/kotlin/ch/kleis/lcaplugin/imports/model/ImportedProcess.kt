package ch.kleis.lcaplugin.imports.model

class ImportedProcess(
    val uid: String
) {
    var meta: MutableMap<String, String?> = mutableMapOf()
    var params: MutableList<ImportedParam> = mutableListOf()
    var comments: MutableList<String> = mutableListOf()

    var productBlocks: MutableList<ExchangeBlock<ImportedProductExchange>> = mutableListOf()
    var inputBlocks: MutableList<ExchangeBlock<ImportedInputExchange>> = mutableListOf()
    var emissionBlocks: MutableList<ExchangeBlock<ImportedBioExchange>> = mutableListOf()
    var resourceBlocks: MutableList<ExchangeBlock<ImportedBioExchange>> = mutableListOf()
    var landUseBlocks: MutableList<ExchangeBlock<ImportedBioExchange>> = mutableListOf()

}

data class ImportedParam(val symbol: String, val value: String)

class ExchangeBlock<T : ImportedExchange>(
    val comment: String?,
    var exchanges: MutableList<T> = mutableListOf()
)

