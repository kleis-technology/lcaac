package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import arrow.optics.Fold
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class ProcessKey(
    val name: String,
    val labels: Map<String, String>,
)

@Serializable
private data class SubstanceKey(
    val name: String,
    val type: String?,
    val compartment: String?,
    val subCompartment: String?,
)

data class SymbolTable(
    val data: Register<DataExpression> = Register.empty(),
    val dimensions: Register<Dimension> = Register.empty(),
    val processTemplates: Register<EProcessTemplate> = Register.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    companion object {
        fun empty() = SymbolTable()
    }


    override fun toString(): String {
        return "[symbolTable]"
    }


    /*
        Templates
     */

    private val processKeyDescriptor = object : IndexKeySerializer<ProcessKey> {
        override fun serialize(key: ProcessKey): String {
            return Json.encodeToString(key)
        }
    }
    private val processKeyOptics = object : Fold<EProcess, ProcessKey> {
        override fun <R> foldMap(M: Monoid<R>, source: EProcess, map: (focus: ProcessKey) -> R): R {
            return map(
                ProcessKey(
                    source.name,
                    source.labels.mapValues { it.value.value }
                )
            )
        }

    }
    private val templatesIndexedByProcessKey: Index<ProcessKey, EProcessTemplate> = Index(
        processTemplates,
        processKeyDescriptor,
        EProcessTemplate.body compose processKeyOptics,
    )

    private val stringDescriptor = object : IndexKeySerializer<String> {
        override fun serialize(key: String): String {
            return key
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

    fun getTemplate(name: String): EProcessTemplate? {
        return templatesIndexedByProcessKey.firstOrNull(
            ProcessKey(
                name,
                emptyMap(),
            )
        )
    }

    fun getTemplate(name: String, labels: Map<String, String>): EProcessTemplate? {
        return templatesIndexedByProcessKey.firstOrNull(
            ProcessKey(
                name, labels
            )
        )
    }

    fun getAllTemplatesByProductName(name: String): List<EProcessTemplate> {
        return templatesIndexedByProductName.getAll(name)
    }


    /*
        Substances
     */
    private val substanceKeyDescriptor = object : IndexKeySerializer<SubstanceKey> {
        override fun serialize(key: SubstanceKey): String {
            return Json.encodeToString(key)
        }
    }
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


    fun getData(name: String): DataExpression? {
        return data[name]
    }

    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String
    ): ESubstanceCharacterization? {
        return substanceCharacterizationsIndexedBySubstanceKey.firstOrNull(
            SubstanceKey(
                name,
                type.value,
                compartment,
                null,
            )
        )
    }

    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String,
        subCompartment: String
    ): ESubstanceCharacterization? {
        return substanceCharacterizationsIndexedBySubstanceKey.firstOrNull(
            SubstanceKey(
                name,
                type.value,
                compartment,
                subCompartment,
            )
        )
    }
}

