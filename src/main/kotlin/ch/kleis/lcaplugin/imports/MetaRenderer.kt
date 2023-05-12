package ch.kleis.lcaplugin.imports

import kotlin.reflect.full.declaredMemberProperties

class MetaRenderer {

    //    @Suppress("UNCHECKED_CAST")
    fun render(o: Any?, prefix: String, metas: MutableMap<String, String>) {
        val realPrefix = if (prefix.isNotBlank()) "${prefix}." else prefix
        when (o) {
            null -> return
            is String -> metas[prefix] = ModelWriter.compactText(o)
            is Map<*, *> -> o.forEach { (k, v) -> render(v, "$realPrefix$k", metas) }
            is Iterable<*> -> o.forEachIndexed { i, v -> render(v, "$realPrefix${i + 1}", metas) }
//            o.javaClass.name.startsWith("spold2") -> {
            else -> {
                o::class.declaredMemberProperties
                    .forEach { p ->
                        val temp = p.getter.call(o)
                        render(temp, "$realPrefix${p.name}", metas)
                    }
            }
//            else -> render(o.toString(), "$realPrefix${i + 1}", metas)
        }
    }
}