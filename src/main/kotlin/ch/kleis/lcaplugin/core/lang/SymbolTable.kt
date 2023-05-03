package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SubstanceKey(
    val name: String,
    val type: String?,
    val compartment: String?,
    val subCompartment: String?,
)

private interface IndexKeyDescriptors {
    companion object {
        val StringDescriptor = object : IndexKeyDescriptor<String> {
            override fun serialize(key: String): String {
                return key
            }

            override fun deserialize(s: String): String {
                return s
            }
        }

        val SubstanceKeyDescriptor = object : IndexKeyDescriptor<SubstanceKey> {
            override fun serialize(key: SubstanceKey): String {
                return Json.encodeToString(key)
            }

            override fun deserialize(s: String): SubstanceKey {
                return Json.decodeFromString(s)
            }
        }
    }
}

data class SymbolTable(
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<EProcessTemplate> = Register.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    private val templatesIndexedByProductName: Index<String, EProcessTemplate> = Index(
        processTemplates,
        IndexKeyDescriptors.StringDescriptor,
        EProcessTemplate.body.products compose
                Every.list() compose
                ETechnoExchange.product compose
                EProductSpec.name
    )
    private val substanceKeyOptics = object : Every<ESubstanceSpec, SubstanceKey> {
        override fun <R> foldMap(M: Monoid<R>, source: ESubstanceSpec, map: (focus: SubstanceKey) -> R): R {
            return map(
                SubstanceKey(
                    source.name,
                    source.type?.value,
                    source.compartment,
                    source.subCompartment,
                )
            )
        }

        override fun modify(source: ESubstanceSpec, map: (focus: SubstanceKey) -> SubstanceKey): ESubstanceSpec {
            val srcKey = SubstanceKey(
                source.name,
                source.type?.value,
                source.compartment,
                source.subCompartment,
            )
            val dstKey = map(srcKey)
            return ESubstanceSpec(
                dstKey.name,
                source.displayName,
                dstKey.type?.let(SubstanceType::of),
                dstKey.compartment,
                dstKey.subCompartment,
                source.referenceUnit,
            )
        }
    }
    private val substanceCharacterizationsIndexedBySubstanceKey: Index<SubstanceKey, ESubstanceCharacterization> =
        Index(
            substanceCharacterizations,
            IndexKeyDescriptors.SubstanceKeyDescriptor,
            ESubstanceCharacterization.referenceExchange.substance compose substanceKeyOptics,
        )

    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): EProcessTemplate? {
        return processTemplates[name]
    }

    fun getUnit(name: String): UnitExpression? {
        return units[name]
    }

    fun getQuantity(name: String): QuantityExpression? {
        return quantities[name]
    }

    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String
    ): ESubstanceCharacterization? {
        return substanceCharacterizationsIndexedBySubstanceKey[SubstanceKey(
            name,
            type.value,
            compartment,
            null,
        )]
    }

    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String,
        subCompartment: String
    ): ESubstanceCharacterization? {
        return substanceCharacterizationsIndexedBySubstanceKey[SubstanceKey(
            name,
            type.value,
            compartment,
            subCompartment,
        )]
    }

    fun getTemplateFromProductName(name: String): EProcessTemplate? {
        return templatesIndexedByProductName[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

