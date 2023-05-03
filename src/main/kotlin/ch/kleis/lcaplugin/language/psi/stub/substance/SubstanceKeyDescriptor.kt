package ch.kleis.lcaplugin.language.psi.stub.substance

import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInput
import java.io.DataOutput

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

    override fun save(storage: DataOutput, value: SubstanceKey) {
        val content = Json.encodeToString(value)
        IOUtil.writeUTF(storage, content)
    }

    override fun read(storage: DataInput): SubstanceKey {
        val content = IOUtil.readUTF(storage)
        return Json.decodeFromString(content)
    }
}
