package ch.kleis.lcaac.core.lang.dimension

import kotlin.math.round

internal fun simpleDimToString(basic: Map.Entry<String, Double>): String {
    return if (basic.value == 1.0) {
        basic.key
    } else {
        val power =
            if (basic.value == round(basic.value)) {
                toPower(String.format("%d", basic.value.toLong()))
            } else {
                "^[${basic.value}]"
            }
        "${basic.key}$power"
    }
}

internal fun multiply(elements: Map<String, Double>, other: Map<String, Double>): Map<String, Double> {
    val es = HashMap<String, Double>(elements)
    other.entries.forEach { entry ->
        es[entry.key] = es[entry.key]?.let { it + entry.value }
            ?: entry.value
    }
    return es
}

internal fun divide(elements: Map<String, Double>, other: Map<String, Double>): Map<String, Double> {
    val es = HashMap<String, Double>(elements)
    other.entries.forEach { entry ->
        es[entry.key] = es[entry.key]?.let { it - entry.value }
            ?: (-entry.value)
    }
    return es
}

internal fun pow(elements: Map<String, Double>, n: Double): Map<String, Double> {
    val es = HashMap<String, Double>()
    elements.entries.forEach { entry ->
        es[entry.key] = n * entry.value
    }
    return es
}

internal fun toPower(f: String): String? {
    return f.map { convert(it) }
        .reduce { strAcc, char ->
            strAcc?.let { str ->
                char?.let { c ->
                    str.plus(c)
                }
            }
        }
}

internal fun convert(c: Char): String? {
    val result: Int? = when (c) {
        '0' -> 0x2070
        '1' -> 0x00B9
        '2' -> 0x00B2
        '3' -> 0x00B3
        in '4'..'9' -> 0x2070 + (c.code - 48)
        '.' -> 0x02D9
        '-' -> 0x207B
        else -> null
    }
    return result?.let { Character.toString(it) }
}
