package ch.kleis.lcaplugin

import java.lang.reflect.Field

class TestUtils {

    companion object {
        fun setField(target: Any, name: String, value: Any?) {
            val clazz = target.javaClass
            val privateField: Field = clazz.getDeclaredField(name)
            privateField.isAccessible = true
            privateField.set(target, value)
        }
    }
}