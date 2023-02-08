package ch.kleis.lcaplugin.language.find_usages

import com.intellij.usages.impl.rules.UsageType

class LcaUsageType {
    companion object {
        val INPUT = UsageType { "Technosphere - Input" }
        val PRODUCT = UsageType { "Technosphere - Product" }
        val BIO_EXCHANGE = UsageType { "Biosphere"}
    }
}
