package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

@optics
sealed interface Value {
    companion object
}

