package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

data class Environment(
    val products: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    val substances: Register<LcaSubstanceExpression> = Register.empty(),
    val indicators: Register<LcaIndicatorExpression> = Register.empty(),
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<TemplateExpression> = Register.empty(),
    val substanceCharacterizations: Register<LcaSubstanceCharacterizationExpression> = Register.empty(),
) {
    companion object {
        fun empty() = Environment()
    }

    fun getTemplate(name: String): TemplateExpression? {
        return processTemplates[name]
    }

    fun getSubstanceCharacterization(name: String): LcaSubstanceCharacterizationExpression? {
        return substanceCharacterizations[name]
    }
}

class Register<E>(
    data: Map<String, E> = HashMap()
) {
    private val data = HashMap(data)

    constructor(register: Register<E>) : this(register.data)

    companion object {
        fun <E> empty(): Register<E> {
            return Register()
        }
    }

    operator fun get(key: String): E? {
        return data[key]
    }

    operator fun set(key: String, e: E) {
        if (data.containsKey(key)) {
            throw EvaluatorException("reference $key already bound: $key = ${data[key]}")
        }
        data[key] = e
    }

    fun keys(): Set<String> {
        return data.keys
    }

    fun entries(): Set<Map.Entry<String, E>> {
        return data.entries
    }
}
