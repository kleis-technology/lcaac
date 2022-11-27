package ch.kleis.lcaplugin.lib.registry

class URN(
    val id: String,
    private val parent: Namespace
) {
    val uid: String = listOf(
        parent.toString(),
        id
    ).joinToString("/")

    val selfNs = parent.ns(id)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as URN

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}
