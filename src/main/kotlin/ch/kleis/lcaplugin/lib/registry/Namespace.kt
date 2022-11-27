package ch.kleis.lcaplugin.lib.registry

class Namespace(
    val id: String,
    private val parent: Namespace?,
) {
    private val localNamespaces = HashMap<String, Namespace>()
    private val localUrns = HashMap<String, URN>()
    val uid: String = path().joinToString("/") { it.id }

    companion object {
        val ROOT = Namespace("", null)
    }

    fun ns(id: String): Namespace {
        return localNamespaces.getOrPut(id) { Namespace(id, this) }
    }

    fun urn(id: String): URN {
        return localUrns.getOrPut(id) { URN(id, this) }
    }

    fun resolve(candidate: String): URN? {
        return localUrns.getOrElse(candidate) {
            parent?.resolve(candidate)
        }
    }

    override fun toString(): String {
        return uid
    }

    private fun path(): List<Namespace> {
        val parentPath = parent?.path() ?: emptyList()
        return parentPath + listOf(this)
    }
}
