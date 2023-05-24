package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.imports.ModelWriter
import org.apache.commons.csv.CSVRecord

data class SubstanceKey(
    var name: String,
    var type: String,
    var compartment: String,
    var subCompartment: String? = null,
    val hasChanged: Boolean = false
) {

    init {
        name = ModelWriter.sanitizeAndCompact(name)
        compartment = compartment.lowercase()
        subCompartment = subCompartment?.ifBlank { null }
    }

    constructor(record: CSVRecord) : this(
        record["Name"],
        record["Type"],
        record["Compartment"],
        record["SubCompartment"].ifBlank { null }
    )

    fun withoutSub(): SubstanceKey {
        return SubstanceKey(this.name, this.type, this.compartment, null, hasChanged = true)
    }

    fun sub(subCompartment: String): SubstanceKey {
        return SubstanceKey(this.name, this.type, this.compartment, subCompartment, hasChanged = true)
    }

    fun removeFromName(toReplace: String): SubstanceKey {
        val newName = this.name.replace("_${toReplace}", "")
        return SubstanceKey(newName, this.type, this.compartment, subCompartment, hasChanged = true)
    }

    fun uid(): String {
        return ModelWriter.sanitizeAndCompact(name)
    }

    /* Need custom implementation to ignore hasChanged field */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubstanceKey

        return name == other.name
            && type == other.type
            && compartment == other.compartment
            && subCompartment == other.subCompartment
    }

    /* Need custom implementation to ignore hasChanged field */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + compartment.hashCode()
        result = 31 * result + (subCompartment?.hashCode() ?: 0)
        return result
    }


}
