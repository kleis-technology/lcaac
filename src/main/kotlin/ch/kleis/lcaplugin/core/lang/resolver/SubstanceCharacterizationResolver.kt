package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver(
    private val symbolTable: SymbolTable,
) {
    fun resolve(spec: ESubstanceSpec): ESubstanceCharacterization? {
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null

        return spec.subcompartment?.let { subCompartment ->
            symbolTable.getSubstanceCharacterization(name, type, compartment, subCompartment)
        } ?: symbolTable.getSubstanceCharacterization(name, type, compartment)
    }
}
