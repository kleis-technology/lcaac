package ch.kleis.lcaplugin.language.find_usages

import com.intellij.usages.impl.rules.UsageType

class LcaUsageType {
    companion object {
        val TECHNOSPHERE_INPUT = UsageType { "Technosphere - Input" }
        val TECHNOSPHERE_PRODUCT = UsageType { "Technosphere - Product" }
        val BIOSPHERE = UsageType { "Biosphere"}
        val SUBSTANCE = UsageType { "Substance" }
    }
}
