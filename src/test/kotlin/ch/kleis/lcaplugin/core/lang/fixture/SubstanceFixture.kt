package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.core.lang.expression.UnitExpression

class SubstanceFixture {
    companion object {
        val propanol = ESubstanceSpec(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            UnitFixture.kg,
        )

        fun substanceWithDefaults(
            name: String = "propanol",
            displayName: String? = "propanol",
            type: SubstanceType? = SubstanceType.RESOURCE,
            compartment: String? = "air",
            subCompartment: String? = null,
            referenceUnit: UnitExpression? = UnitFixture.kg
        ): ESubstanceSpec =
            ESubstanceSpec(
                "propanol",
                "propanol",
                type = SubstanceType.RESOURCE,
                compartment,
                null,
                UnitFixture.kg,
            )

    }
}