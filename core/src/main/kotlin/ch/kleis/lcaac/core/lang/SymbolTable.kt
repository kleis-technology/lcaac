package ch.kleis.lcaac.core.lang

import arrow.optics.Every
import arrow.optics.Fold
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.*


data class SymbolTable<Q>(
    val data: DataRegister<Q> = DataRegister.empty(),
    val dimensions: DimensionRegister = DimensionRegister.empty(),
    val processTemplates: ProcessTemplateRegister<Q> = ProcessTemplateRegister.empty(),
    val substanceCharacterizations: SubstanceCharacterizationRegister<Q> = SubstanceCharacterizationRegister.empty(),
) {
    companion object {
        fun <Q> empty() = SymbolTable<Q>()
    }


    override fun toString(): String {
        return "[symbolTable]"
    }


    /*
        Templates
     */

    @Suppress("LocalVariableName")
    private val processKeyOptics = object : Fold<EProcess<Q>, ProcessKey> {
        override fun <R> foldMap(M: Monoid<R>, source: EProcess<Q>, map: (focus: ProcessKey) -> R): R {
            return map(
                ProcessKey(
                    source.name,
                    source.labels.mapValues { it.value.value }
                )
            )
        }

    }
    private val templatesIndexedByProcessKey: Index<ProcessKey, ProcessKey, EProcessTemplate<Q>> = Index(
        processTemplates,
        EProcessTemplate.body<Q>() compose processKeyOptics,
    )


    private val templatesIndexedByProductName: Index<String, ProcessKey, EProcessTemplate<Q>> = Index(
        processTemplates,
        EProcessTemplate.body<Q>().products() compose
            Every.list() compose
            ETechnoExchange.product() compose
            EProductSpec.name()
    )

    fun getTemplate(name: String): EProcessTemplate<Q>? {
        return templatesIndexedByProcessKey.firstOrNull(
            ProcessKey(
                name,
                emptyMap(),
            )
        )
    }

    fun getTemplate(name: String, labels: Map<String, String>): EProcessTemplate<Q>? {
        return templatesIndexedByProcessKey.firstOrNull(
            ProcessKey(
                name, labels
            )
        )
    }

    fun getAllTemplatesByProductName(name: String): List<EProcessTemplate<Q>> {
        return templatesIndexedByProductName.getAll(name)
    }


    /*
        Substances
     */
    @Suppress("LocalVariableName")
    private val substanceKeyOptics = object : Fold<ESubstanceSpec<Q>, SubstanceKey> {
        override fun <R> foldMap(M: Monoid<R>, source: ESubstanceSpec<Q>, map: (focus: SubstanceKey) -> R): R {
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
    private val substanceCharacterizationsIndexedBySubstanceKey: Index<SubstanceKey, SubstanceKey, ESubstanceCharacterization<Q>> =
        Index(
            substanceCharacterizations,
            ESubstanceCharacterization.referenceExchange<Q>().substance() compose substanceKeyOptics,
        )


    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String
    ): ESubstanceCharacterization<Q>? {
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
    ): ESubstanceCharacterization<Q>? {
        return substanceCharacterizationsIndexedBySubstanceKey.firstOrNull(
            SubstanceKey(
                name,
                type.value,
                compartment,
                subCompartment,
            )
        )
    }

    /*
        Data
     */
    fun getData(name: String): DataExpression<Q>? {
        return data[DataKey(name)]
    }

}

