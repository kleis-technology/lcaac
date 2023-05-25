package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import arrow.optics.Fold
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SubstanceKey(
    val name: String,
    val type: String?,
    val compartment: String?,
    val subCompartment: String?,
)

data class SymbolTable(
    val quantities: Register<QuantityExpression> = Register.empty(),
    val dimensions: Register<Dimension> = Register.empty(),
    val processTemplates: Register<EProcessTemplate> = Register.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    private val stringDescriptor = object : IndexKeySerializer<String> {
        override fun serialize(key: String): String {
            return key
        }
    }

    private val substanceKeyDescriptor = object : IndexKeySerializer<SubstanceKey> {
        override fun serialize(key: SubstanceKey): String {
            return Json.encodeToString(key)
        }
    }
    private val templatesIndexedByProductName: Index<String, EProcessTemplate> = Index(
        processTemplates,
        stringDescriptor,
        EProcessTemplate.body.products compose
            Every.list() compose
            ETechnoExchange.product compose
            EProductSpec.name
    )
    private val substanceKeyOptics = object : Fold<ESubstanceSpec, SubstanceKey> {
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
    }
    private val substanceCharacterizationsIndexedBySubstanceKey: Index<SubstanceKey, ESubstanceCharacterization> =
        Index(
            substanceCharacterizations,
            substanceKeyDescriptor,
            ESubstanceCharacterization.referenceExchange.substance compose substanceKeyOptics,
        )

    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): EProcessTemplate? {
        return processTemplates[name]
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

