package ch.kleis.lcaplugin.imports.shared

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.SubstanceImported

class SubstanceSerializer {

    companion object {

        fun serialize(s: SubstanceImported): CharSequence {

            val builder = StringBuilder()

            builder.append(
                """

substance ${s.uid} {

    name = "${s.name}"
    type = ${s.type}
    compartment = "${s.compartment}""""
            )
            if (!s.subCompartment.isNullOrBlank()) {
                builder.append(
                    """
    sub_compartment = "${s.subCompartment}""""
                )
            }
            builder.append(
                """
    reference_unit = ${s.referenceUnit}

    impacts {"""
            )
            s.impacts.forEach {
                builder.append(
                    """
        ${it.value} ${it.unit} ${it.uid}"""
                )
            }
            builder.append(
                """
    }

    meta {
"""
            )
            builder.append(ModelWriter.blockKeyValue(s.meta.entries, 8))
            builder.append(
                """
    }
}"""
            )
            return builder
        }
    }
}