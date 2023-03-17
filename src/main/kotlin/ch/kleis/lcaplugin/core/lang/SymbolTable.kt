package ch.kleis.lcaplugin.core.lang

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

@optics
data class SymbolTable(
    val products: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    val substances: Register<LcaSubstanceExpression> = Register.empty(),
    val indicators: Register<LcaIndicatorExpression> = Register.empty(),
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<TemplateExpression> = Register.empty(),
    val substanceCharacterizations: Register<LcaSubstanceCharacterizationExpression> = Register.empty(),
) {
    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): TemplateExpression? {
        return processTemplates[name]
    }

    fun getSubstanceCharacterization(name: String): LcaSubstanceCharacterizationExpression? {
        return substanceCharacterizations[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

class Register<E>(
    data: Map<String, E> = HashMap()
) : MutableMap<String, E> {
    private val data = HashMap(data)

    constructor(register: Register<E>) : this(register.data)
    constructor(vararg pairs: Pair<String, E>) : this(mapOf(*pairs))

    companion object {
        fun <E> empty(): Register<E> {
            return Register()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, E>>
        get() = data.entries
    override val keys: MutableSet<String>
        get() = data.keys
    override val size: Int
        get() = data.size
    override val values: MutableCollection<E>
        get() = data.values

    override fun clear() {
        data.clear()
    }

    override fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun remove(key: String): E? {
        return data.remove(key)
    }

    override fun putAll(from: Map<out String, E>) {
        val conflicts = from.keys
            .filter { data.containsKey(it) }
        if (conflicts.isNotEmpty()) {
            throw EvaluatorException("$conflicts are already bound")
        }
        return data.putAll(from)
    }

    override fun put(key: String, value: E): E? {
        if (data.containsKey(key)) {
            throw EvaluatorException("reference $key already bound: $key = ${data[key]}")
        }
        return data.put(key, value)
    }

    override fun get(key: String): E? {
        return data[key]
    }

    override fun containsValue(value: E): Boolean {
        return data.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return data.containsKey(key)
    }

    override fun toString(): String {
        return "[register<${this.javaClass.simpleName}>]"
    }
}
