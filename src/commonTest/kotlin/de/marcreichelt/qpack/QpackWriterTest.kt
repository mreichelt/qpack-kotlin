package de.marcreichelt.qpack

import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import kotlin.test.Test
import kotlin.test.assertEquals

// test examples from https://quiche.googlesource.com/quiche/+/refs/heads/master/quic/core/qpack/qpack_encoder_test.cc

class QpackWriterTest {

    @Test
    fun writeSingleByteIntZero() {
        val buffer = Buffer()
        encodeInteger(buffer, value = 0, 0x00, 8)
        assertEquals("00".hex, buffer.readByteString())
    }

    @Test
    fun writeSingleByteInt() {
        val buffer = Buffer()
        encodeInteger(buffer, value = 42, 0x00, 8)
        assertEquals("2a".hex, buffer.readByteString())
    }

    @Test
    fun writeSingleByteWithPrefix() {
        val buffer = Buffer()
        encodeInteger(buffer, value = 0b00111111, prefix = 0b10000000, 7)
        assertEquals(int2Bytes(0b10111111), buffer.readByteString())
    }

    @Test
    fun writeTwoByteWithPrefix() {
        val buffer = Buffer()
        encodeInteger(buffer, value = 0xffff, 0b10000000, 7)
        // 0xffff = 65535
        // 2^N - 1 = 127
        // 65535 - 127 = 65408
        // 65408 = 0b11111111_10000000
        // encoded in reverse order: least significant 7 bits (0000000) first, then next 7 (1111111), then most significant 7 bits (0000011)
        // all octets start with 1, last starts with 0:     0b10000000                   0b11111111                              0b00000011
        //                             1           1           0
        assertEquals(int2Bytes(0xff, 0b10000000, 0b11111111, 0b00000011), buffer.readByteString())

        // same as above, in hex
        encodeInteger(buffer, value = 0xffff, 0x80, 7)
        assertEquals("ff80ff03".hex, buffer.readByteString())
    }

    @Test
    fun writeEmptyList() {
        assertEquals("0000".hex, write(emptyList()))
    }
//
//    @Test
//    fun emptyName() {
//        val headers = listOf(Header("", "foo"))
//        assertEquals("0000208294e7".hex, write(headers))
//    }
//
//    @Test
//    fun emptyValue() {
//        val headers = listOf(Header("foo", ""))
//        assertEquals("00002a94e700".hex, write(headers))
//    }

    private val String.hex: ByteString
        get() = decodeHex()

    private fun int2Bytes(vararg ints: Int): ByteString {
        return ints.map { it.toByte() }.toByteArray().toByteString()
    }

    private fun write(headers: List<Header>): ByteString {
        val writer = Qpack.Writer(
            out = Buffer(),
            encoderBuffer = Buffer(),
            maxBlockedStreams = 1,
            acknowledgementMode = AcknowledgementMode.None
        )
        return writer.encode(headers)
    }

}
