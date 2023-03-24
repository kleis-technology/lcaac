package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.Dimension

@optics
sealed interface Value {
    companion object
}

