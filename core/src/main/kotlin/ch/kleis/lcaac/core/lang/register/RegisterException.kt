package ch.kleis.lcaac.core.lang.register

data class RegisterException(val duplicates: Set<String>) : Exception(
    "$duplicates ${
        if (duplicates.size > 1) {
            "are"
        } else {
            "is"
        }
    } already bound"
)
