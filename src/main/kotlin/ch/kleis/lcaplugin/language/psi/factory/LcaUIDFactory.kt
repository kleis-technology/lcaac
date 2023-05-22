package ch.kleis.lcaplugin.language.psi.factory

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiUID

class LcaUIDFactory(
    private val createFile: (content: String) -> LcaFile
) {
    fun createUid(name: String): PsiUID {
        val content = """
            package dummy
            variables {
                $name = 1 kg
            }
        """.trimIndent()
        val file = createFile(content)
        val varBlock = file.getPsiGlobalVariablesBlocks().first()
        val assignment = varBlock.getGlobalAssignments().first()
        return assignment.getQuantityRef().getUID()
    }
}
