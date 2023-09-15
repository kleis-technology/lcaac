package ch.kleis.lcaac.grammar

data class LoaderException(override val message: String): Exception(message)
