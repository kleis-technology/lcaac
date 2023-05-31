package ch.kleis.lcaplugin.language.psi.stub.process

import com.intellij.util.io.KeyDescriptor
import java.io.*

class ProcessKeyDescriptor : KeyDescriptor<ProcessKey> {
    companion object {
        val INSTANCE = ProcessKeyDescriptor()
    }

    override fun getHashCode(value: ProcessKey?): Int {
        return value.hashCode()
    }

    override fun isEqual(val1: ProcessKey?, val2: ProcessKey?): Boolean {
        return val1 == val2
    }

    override fun save(storage: DataOutput, value: ProcessKey?) {
        ObjectOutputStream(storage as OutputStream).writeObject(value)
    }

    override fun read(storage: DataInput): ProcessKey =
        ObjectInputStream(storage as InputStream).readObject() as ProcessKey
}
