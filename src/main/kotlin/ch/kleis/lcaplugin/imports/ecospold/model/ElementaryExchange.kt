package ch.kleis.lcaplugin.imports.ecospold.model

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

data class ElementaryExchange(
        val elementaryExchangeId: String,
        val amount: Double,
        val name: String,
        val unit: String,
        val compartment: String,
        val subCompartment: String?,
        val substanceType: SubstanceType,
        val comment: String?,
)