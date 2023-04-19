package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.imports.ModelWriter
import org.apache.commons.csv.CSVRecord

// TODO Virer Type qui n'est pas dsnas la clé
data class SubstanceKey(
    val name: String,
    val type: String, // Better to not use the enum type SubstanceType here in case type is not valid
    val compartment: String,
    val subCompartment: String?
) {
    fun withoutSubCompartment(): SubstanceKey {
        return SubstanceKey(this.name, this.type, this.compartment, null)
    }

    constructor(record: CSVRecord) : this(
        record["Name"],
        record["Type"],
        record["Compartment"],
        record["SubCompartment"].ifBlank { null }
    )

    fun uid(): String {
        val option = if (subCompartment.isNullOrBlank()) "" else "_$subCompartment"
        return ModelWriter.sanitizeAndCompact("${name}_${type}_$compartment$option")
    }
}