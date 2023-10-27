package ch.kleis.lcaac.core.lang

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.*


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

    private val templatesIndexedByProductName: Index<String, ProcessKey, EProcessTemplate<Q>> = Index(
        processTemplates,
        EProcessTemplate.body<Q>().products() compose
            Every.list() compose
            ETechnoExchange.product() compose
            EProductSpec.name()
    )

    fun getTemplate(name: String): EProcessTemplate<Q>? {
        return processTemplates[ProcessKey(name)]
    }

    fun getTemplate(name: String, labels: Map<String, String>): EProcessTemplate<Q>? {
        return processTemplates[ProcessKey(name, labels)]
    }

    fun getAllTemplatesByProductName(name: String): List<EProcessTemplate<Q>> {
        return templatesIndexedByProductName.getAll(name)
    }


    /*
        Substances
     */
    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String,
        subCompartment: String? = null,
    ): ESubstanceCharacterization<Q>? {
        return substanceCharacterizations[SubstanceKey(name, type, compartment, subCompartment)]
    }

    /*
        Data
     */
    fun getData(name: String): DataExpression<Q>? {
        return data[DataKey(name)]
    }

}

