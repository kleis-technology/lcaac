package com.github.albanseurat.lcaplugin.services.formatter

data class TextLine(val content: String): TextRegion {
    override fun getLines(): List<String> {
        return listOf(content)
    }
}
