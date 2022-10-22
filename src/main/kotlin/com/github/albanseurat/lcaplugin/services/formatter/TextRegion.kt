package com.github.albanseurat.lcaplugin.services.formatter

sealed interface TextRegion {
    fun getLines(): List<String>
    fun render(): String {
        return getLines().joinToString("\n")
    }
}
