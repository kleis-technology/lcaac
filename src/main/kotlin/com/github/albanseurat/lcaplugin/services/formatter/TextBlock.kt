package com.github.albanseurat.lcaplugin.services.formatter

data class TextBlock(val regions: List<TextRegion>): TextRegion {
    override fun getLines(): List<String> {
        return regions.stream()
            .flatMap { it.getLines().stream() }
            .toList()
    }
}
