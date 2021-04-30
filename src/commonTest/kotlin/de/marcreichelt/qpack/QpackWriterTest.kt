package de.marcreichelt.qpack

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import kotlin.test.Test
import kotlin.test.assertEquals

// test examples from https://quiche.googlesource.com/quiche/+/refs/heads/master/quic/core/qpack/qpack_encoder_test.cc

class QpackWriterTest {

    @Test
    fun writeEmptyList() {
        assertEquals("0000".hex, write(emptyList()))
    }

    private val String.hex: ByteString
        get() = decodeHex()

    private fun write(headers: List<Header>): ByteString {
        val writer = Qpack.Writer(
            maxBlockedStreams = 1,
            acknowledgementMode = AcknowledgementMode.None
        )
        return writer.encode(QuicStreamId(1), headers)
    }

}