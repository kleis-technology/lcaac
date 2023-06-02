package ch.kleis.lcaplugin.language.type_checker

import com.intellij.psi.PsiElement

internal class RecursiveGuard {
    private val visited = HashSet<PsiElement>()

    fun <E : PsiElement, R> guard(fn: (E) -> R): (E) -> R {
        return { element ->
            if (visited.contains(element)) {
                val names = visited.map {
                    """"${
                        it.text
                            .replace("\n", "")
                            .replace("\\s+".toRegex(), " ")
                            .take(20)
                    } ...""""
                }.sorted().joinToString().take(80)
                throw PsiTypeCheckException("circular dependencies: $names")
            }
            visited.add(element)
            val r = fn(element)
            visited.remove(element)
            r
        }
    }
}
