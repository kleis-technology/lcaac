package ch.kleis.lcaplugin.language.psi.stub.substance

import com.intellij.util.io.KeyDescriptor
import java.io.*

class SubstanceKeyDescriptor : KeyDescriptor<SubstanceKey> {
    companion object {
        val INSTANCE = SubstanceKeyDescriptor()
    }

    override fun getHashCode(value: SubstanceKey): Int {
        return value.hashCode()
    }

    override fun isEqual(val1: SubstanceKey, val2: SubstanceKey): Boolean {
        return val1 == val2
    }

    /* Though we have no theoretical guarantee that storage implements OutputStream, it is practically always the
     * case (see com/intellij/psi/stubs/SerializedStubTree.java), and the alternative is to wrap a
     * ByteArrayOutputStream with an ObjectOutputStream and then copy the bytes to the storage - slow, and rather
     * unreadable. See also the read method.
     */
    override fun save(storage: DataOutput, value: SubstanceKey) {
        ObjectOutputStream(storage as OutputStream).writeObject(value)
    }

    /* Closing the streams manipulated here causes failures in the intellij stub tree manipulation further down,
     * hence the absence of try-with-resources.
     *
     * Though we have no theoretical guarantee that storage implements InputStream, it is practically always the case
     * (c.f. above), and the alternative requires reading byte-by-byte wrapped in a try/catch with several intermediate
     * buffers/XXXInputStreams. @jde, @pbl and @pke agreed this was a better solution - talk with them if need be.
     */
    override fun read(storage: DataInput): SubstanceKey =
        ObjectInputStream(storage as InputStream).readObject() as SubstanceKey
}
