package ch.kleis.lcaplugin.services.formatter

data class TextIndent(val regions: List<TextRegion>, val indent: Int = 4): TextRegion {
    override fun getLines(): List<String> {
        return regions.stream()
            .flatMap { it.getLines().stream() }
            .map { " ".repeat(indent) + it }
            .toList()
    }
}
