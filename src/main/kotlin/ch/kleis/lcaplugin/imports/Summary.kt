package ch.kleis.lcaplugin.imports

data class Imported(val qty: Int, val name: String)

sealed class Summary(
    val durationInSec: Long,
    private val importedResources: List<Imported> = listOf()
) {
    fun getResourcesAsString(): String {
        return importedResources.filter { it.qty > 0 }
            .joinToString(", ") { "${it.qty} ${it.name}" }
            .ifEmpty { "nothing imported." }
    }
}

class SummaryInSuccess(
    durationInSec: Long,
    importedResources: List<Imported>,
) : Summary(durationInSec, importedResources)

class SummaryInterrupted(
    durationInSec: Long,
    importedResources: List<Imported>,
) : Summary(durationInSec, importedResources)

class SummaryInError(
    durationInSec: Long,
    importedResources: List<Imported>,
    val errorMessage: String
) : Summary(durationInSec, importedResources)
