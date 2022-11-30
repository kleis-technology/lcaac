package ch.kleis.lcaplugin.lib.urn

class Namespace(
    val id: String,
    val parent: Namespace?,
) {
    private val localNamespaces = HashMap<String, Namespace>()
    private val localUrns = HashMap<String, URN>()
    val uid: String = path()
        .joinToString(SEPARATOR) { it.id }

    companion object {
        val ROOT = Namespace("", null)
        const val SEPARATOR = "."
    }

    fun append(urn: URN): URN {
        var ns = this
        urn.path().forEach {
            ns = ns.ns(it.id)
        }
        return ns.urn(urn.id)
    }

    fun ns(id: String): Namespace {
        return localNamespaces.getOrPut(id) { Namespace(id, this) }
    }

    fun ns(parts: List<String>): Namespace {
        var ns = this
        for (part in parts) {
            ns = ns.ns(part)
        }
        return ns
    }

    fun urn(id: String): URN {
        return localUrns.getOrPut(id) { URN(id, this) }
    }

    fun urn(parts: List<String>): URN {
        var ns = this
        for (part in parts) {
            ns = ns.ns(part)
        }
        val last = parts.last()
        return ns.parent?.urn(last) ?: this.urn(last)
    }

    fun selfUrn(): URN {
        return parent?.urn(id) ?: urn(id)
    }

    fun resolve(candidate: String): URN? {
        return localUrns.getOrElse(candidate) {
            parent?.resolve(candidate)
        }
    }

    override fun toString(): String {
        return uid
    }

    fun path(): List<Namespace> {
        val parentPath = parent?.path() ?: emptyList()
        return parentPath + listOf(this)
    }
}
