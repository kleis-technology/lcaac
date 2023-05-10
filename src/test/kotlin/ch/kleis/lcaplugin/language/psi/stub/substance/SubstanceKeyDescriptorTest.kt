package ch.kleis.lcaplugin.language.psi.stub.substance

import org.junit.Test
import java.io.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class SubstanceKeyDescriptorTest {

    private fun toDataInput(buffer: ByteArrayOutputStream): DataInput =
            DataInputStream(ByteArrayInputStream(buffer.toByteArray()))

    @Test
    fun test_whenSaveThenRead_thenIsEqual() {
        // Given
        val substanceKey = SubstanceKey("alpha", "bravo", "charlie", "delta")
        val buffer = ByteArrayOutputStream()

        val storageToBuffer: DataOutput = DataOutputStream(buffer)
        val sut: SubstanceKeyDescriptor = SubstanceKeyDescriptor.INSTANCE

        // When
        sut.save(storageToBuffer, substanceKey)

        // Then
        assertEquals(substanceKey, sut.read(toDataInput(buffer)))
    }

    @Test
    fun test_whenDifferentSubstanceKey_thenIsDifferent() {
        // given
        val sk1 = SubstanceKey("alpha", "bravo", "charlie", "delta")
        val sk2 = SubstanceKey("zulu", "xray", "yankee", null)

        val b1 = ByteArrayOutputStream()
        val b2 = ByteArrayOutputStream()

        val storageAsOutput1: DataOutput = DataOutputStream(b1)
        val storageAsOutput2: DataOutput = DataOutputStream(b2)

        val sut: SubstanceKeyDescriptor = SubstanceKeyDescriptor.INSTANCE

        // when
        sut.save(storageAsOutput1, sk1)
        sut.save(storageAsOutput2, sk2)

        // then
        assertNotEquals(b1.toByteArray(), b2.toByteArray())
    }

    @Test
    fun test_whenSameSubstanceKey_thenSerializeSame() {
        // given
        val sk1 = SubstanceKey("alpha", "bravo", "charlie", "delta")
        val sk2 = SubstanceKey("alpha", "bravo", "charlie", "delta")

        val b1 = ByteArrayOutputStream()
        val b2 = ByteArrayOutputStream()

        var nextByte1: Result<Byte>
        var nextByte2: Result<Byte>

        val storageAsOutput1: DataOutput = DataOutputStream(b1)
        val storageAsOutput2: DataOutput = DataOutputStream(b2)

        val sut: SubstanceKeyDescriptor = SubstanceKeyDescriptor.INSTANCE

        // when
        sut.save(storageAsOutput1, sk1)
        sut.save(storageAsOutput2, sk2)

        val storageAsInput1 = toDataInput(b1)
        val storageAsInput2 = toDataInput(b2)

        // then
        do {
            nextByte1 = storageAsInput1.runCatching { this.readByte() }
            nextByte2 = storageAsInput2.runCatching { this.readByte() }
            assertEquals(nextByte1.getOrNull(), nextByte2.getOrNull())
        } while (nextByte1.isSuccess && nextByte2.isSuccess)
    }
}