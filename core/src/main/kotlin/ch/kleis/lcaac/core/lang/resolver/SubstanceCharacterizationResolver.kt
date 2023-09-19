package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver<Q>(
    private val symbolTable: SymbolTable<Q>,
) {
    fun resolve(spec: ESubstanceSpec<Q>): ESubstanceCharacterization<Q>? {
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null

        return spec.subCompartment?.let { subCompartment ->
            symbolTable.getSubstanceCharacterization(name, type, compartment, subCompartment)
        } ?: symbolTable.getSubstanceCharacterization(name, type, compartment)
    }
}
