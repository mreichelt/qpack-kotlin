package de.marcreichelt.qpack

import Huffman
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/** Original version of this class was lifted from `com.twitter.hpack.de.marcreichelt.qpack.HuffmanTest`.  */
class HuffmanTest {

    @Test
    fun roundTripForRequestAndResponse() {
        val s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        for (i in s.indices) {
            assertRoundTrip(s.substring(0, i).encodeUtf8())
        }
        val random = Random(123456789L)
        val buf = ByteArray(4096)
        random.nextBytes(buf)
        assertRoundTrip(buf.toByteString())
    }

    private fun assertRoundTrip(data: ByteString) {
        val encodeBuffer = Buffer()
        Huffman.encode(data, encodeBuffer)
        assertEquals(encodeBuffer.size, Huffman.encodedLength(data).toLong())

        val decodeBuffer = Buffer()
        Huffman.decode(encodeBuffer, encodeBuffer.size, decodeBuffer)
        assertEquals(data, decodeBuffer.readByteString())
    }

}
