package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver(
    private val symbolTable: SymbolTable,
) {
    fun resolve(spec: ESubstanceSpec): ESubstanceCharacterization? {
        return symbolTable.getSubstanceCharacterizationFromSubstanceName(spec.name)
    }
}
