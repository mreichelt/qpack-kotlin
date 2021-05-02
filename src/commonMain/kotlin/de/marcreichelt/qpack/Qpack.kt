package de.marcreichelt.qpack

import de.marcreichelt.qpack.AcknowledgementMode.None
import de.marcreichelt.qpack.Header.Companion.RESPONSE_STATUS
import de.marcreichelt.qpack.Header.Companion.TARGET_AUTHORITY
import de.marcreichelt.qpack.Header.Companion.TARGET_METHOD
import de.marcreichelt.qpack.Header.Companion.TARGET_PATH
import de.marcreichelt.qpack.Header.Companion.TARGET_SCHEME
import okio.Buffer
import okio.ByteString
import kotlin.math.max

inline class QuicStreamId(val value: Int) {
    init {
        require(value >= 1)
    }
}

object Qpack {

    /**
     * Static table as defined in [QPACK](https://quicwg.org/base-drafts/draft-ietf-quic-qpack.html#section-appendix.a)
     */
    val staticTable: List<Header> = listOf(
        Header(TARGET_AUTHORITY, ""),
        Header(TARGET_PATH, "/"),
        Header("age", "0"),
        Header("content-disposition", ""),
        Header("content-length", "0"),
        Header("cookie", ""),
        Header("date", ""),
        Header("etag", ""),
        Header("if-modified-since", ""),
        Header("if-none-match", ""),
        Header("last-modified", ""),
        Header("link", ""),
        Header("location", ""),
        Header("referer", ""),
        Header("set-cookie", ""),
        Header(TARGET_METHOD, "CONNECT"),
        Header(TARGET_METHOD, "DELETE"),
        Header(TARGET_METHOD, "GET"),
        Header(TARGET_METHOD, "HEAD"),
        Header(TARGET_METHOD, "OPTIONS"),
        Header(TARGET_METHOD, "POST"),
        Header(TARGET_METHOD, "PUT"),
        Header(TARGET_SCHEME, "http"),
        Header(TARGET_SCHEME, "https"),
        Header(RESPONSE_STATUS, "103"),
        Header(RESPONSE_STATUS, "200"),
        Header(RESPONSE_STATUS, "304"),
        Header(RESPONSE_STATUS, "404"),
        Header(RESPONSE_STATUS, "503"),
        Header("accept", "*/*"),
        Header("accept", "application/dns-message"),
        Header("accept-encoding", "gzip, deflate, br"),
        Header("accept-ranges", "bytes"),
        Header("access-control-allow-headers", "cache-control"),
        Header("access-control-allow-headers", "content-type"),
        Header("access-control-allow-origin", "*"),
        Header("cache-control", "max-age=0"),
        Header("cache-control", "max-age=2592000"),
        Header("cache-control", "max-age=604800"),
        Header("cache-control", "no-cache"),
        Header("cache-control", "no-store"),
        Header("cache-control", "public, max-age=31536000"),
        Header("content-encoding", "br"),
        Header("content-encoding", "gzip"),
        Header("content-type", "application/dns-message"),
        Header("content-type", "application/javascript"),
        Header("content-type", "application/json"),
        Header("content-type", "application/x-www-form-urlencoded"),
        Header("content-type", "image/gif"),
        Header("content-type", "image/jpeg"),
        Header("content-type", "image/png"),
        Header("content-type", "text/css"),
        Header("content-type", "text/html; charset=utf-8"),
        Header("content-type", "text/plain"),
        Header("content-type", "text/plain;charset=utf-8"),
        Header("range", "bytes=0-"),
        Header("strict-transport-security", "max-age=31536000"),
        Header("strict-transport-security", "max-age=31536000; includesubdomains"),
        Header("strict-transport-security", "max-age=31536000; includesubdomains; preload"),
        Header("vary", "accept-encoding"),
        Header("vary", "origin"),
        Header("x-content-type-option", "nosniff"),
        Header("x-xss-protection", "1; mode=block"),
        Header(RESPONSE_STATUS, "100"),
        Header(RESPONSE_STATUS, "204"),
        Header(RESPONSE_STATUS, "206"),
        Header(RESPONSE_STATUS, "302"),
        Header(RESPONSE_STATUS, "400"),
        Header(RESPONSE_STATUS, "403"),
        Header(RESPONSE_STATUS, "421"),
        Header(RESPONSE_STATUS, "425"),
        Header(RESPONSE_STATUS, "500"),
        Header("accept-language", ""),
        Header("access-control-allow-credentials", "FALSE"),
        Header("access-control-allow-credentials", "TRUE"),
        Header("access-control-allow-headers", "*"),
        Header("access-control-allow-methods", "get"),
        Header("access-control-allow-methods", "get, post, options"),
        Header("access-control-allow-methods", "options"),
        Header("access-control-expose-headers", "content-length"),
        Header("access-control-request-headers", "content-type"),
        Header("access-control-request-method", "get"),
        Header("access-control-request-method", "post"),
        Header("alt-svc", "clear"),
        Header("authorization", ""),
        Header("content-security-policy", "script-src 'none'; object-src 'none'; base-uri 'none'"),
        Header("early-data", "1"),
        Header("expect-ct", ""),
        Header("forwarded", ""),
        Header("if-range", ""),
        Header("origin", ""),
        Header("purpose", "prefetch"),
        Header("server", ""),
        Header("timing-allow-origin", "*"),
        Header("upgrade-insecure-requests", "1"),
        Header("user-agent", ""),
        Header("x-forwarded-for", ""),
        Header("x-frame-options", "deny"),
        Header("x-frame-options", "sameorigin"),
    )

    val dynamicTable: MutableList<Header> = mutableListOf()

    class Writer(
        val out: Buffer,
        val encoderBuffer: Buffer,
        private val maxDynamicTableCapacity: Int = 0,
        val maxBlockedStreams: Int = 0,
        val acknowledgementMode: AcknowledgementMode = None,
    ) {

        fun encode(headers: List<Header>): ByteString {
            val base = dynamicTable.size
            var requiredInsertCount = 0

            headers.forEach { header ->
                val staticIndex = findInStaticTable(header)
                if (staticIndex != -1) {
                    encodeStaticIndexReference(out, staticIndex)
                    return@forEach
                }

                var dynamicIndex = findInDynamicTable(header)
                var staticNameIndex = -1
                var dynamicNameIndex = -1
                if (dynamicIndex == -1) {
                    // No matching entry.  Either insert+index or encode literal
                    staticNameIndex = findNameInStaticTable(header.name)
                    if (staticNameIndex == -1) {
                        dynamicNameIndex = findNameInDynamicTable(header.name)
                    }

                    if (shouldIndex(header) and dynamicTableCanIndex(header)) {
                        encodeInsert(staticNameIndex, dynamicNameIndex, header)
                        dynamicTable += header
                        dynamicIndex = dynamicTable.lastIndex
                    }
                }

                if (dynamicIndex == -1) {
                    // Could not index it, literal
                    if (dynamicNameIndex != -1) {
                        // Encode literal with dynamic name, possibly above base
                        encodeDynamicLiteral(dynamicNameIndex, base, header)
                        requiredInsertCount = max(requiredInsertCount, dynamicNameIndex)
                    } else {
                        // Encodes a literal with a static name or literal name
                        encodeLiteral(staticNameIndex, header)
                    }
                } else {
                    // Dynamic index reference
                    require(dynamicIndex != -1)
                    requiredInsertCount = max(requiredInsertCount, dynamicIndex)
                    // Encode dynamicIndex, possibly above base
                    encodeDynamicIndexReference(dynamicIndex, base)
                }

            }

            // encode the prefix
            val prefixBuffer = Buffer()
            if (requiredInsertCount == 0) {
                encodeInteger(prefixBuffer, 0, 0x00, 8)
                encodeInteger(prefixBuffer, 0, 0x00, 7)
            } else {
                val wireRIC = (requiredInsertCount % (2 * getMaxEntries())) + 1
                encodeInteger(prefixBuffer, wireRIC, 0x00, 8)
                if (base >= requiredInsertCount) {
                    encodeInteger(prefixBuffer, base - requiredInsertCount, 0x00, 7)
                } else {
                    encodeInteger(prefixBuffer, requiredInsertCount - base - 1, 0x80, 7)
                }
            }

            // TODO:
            // return encoderBuffer, prefixBuffer + streamBuffer

            return prefixBuffer.readByteString()
        }

        private fun getMaxEntries(): Int {
            return maxDynamicTableCapacity / 32
        }

        private fun shouldIndex(header: Header) = true // TODO
        private fun dynamicTableCanIndex(header: Header) = true // TODO

        private fun findInStaticTable(header: Header): Int {
            return staticTable.indexOf(header) // TODO: make more efficient
        }

        private fun findNameInStaticTable(name: ByteString): Int {
            return staticTable.indexOfFirst { it.name == name } // TODO: make more efficient
        }

        private fun findInDynamicTable(header: Header): Int {
            return dynamicTable.indexOf(header) // TODO: make more efficient
        }

        private fun findNameInDynamicTable(name: ByteString): Int {
            return staticTable.indexOfFirst { it.name == name } // TODO: make more efficient
        }

    }

}

/**
 * Encodes a prefix integer, as described in [RFC7541 section 5](https://datatracker.ietf.org/doc/html/rfc7541#section-5)
 */
internal fun encodeInteger(buffer: Buffer, value: Int, prefix: Int, prefixLength: Int) {
    require(prefixLength in 1..8) {
        "prefix length must be between 1 and 8"
    }
    require(prefix in 0..255) {
        "prefix must be between 0 and 255"
    }

    val upperBoundForFirstByte = (2 shl prefixLength - 1) - 1 // == 2^N - 1
    if (value < upperBoundForFirstByte) {
        // value fits in single byte , including the prefix mask
        // if prefixLength == 5, this byte will be `pppvvvvv` - first 3 bits of prefix, last bits the value itself
        buffer.writeByte(prefix or value)
        return
    }

    // value does not fit in prefix byte, so fill up prefix with '1'
    buffer.writeByte(prefix or upperBoundForFirstByte)
    var restOfValue = value - upperBoundForFirstByte

    // write least significant 7 bits first, preceded by '1' bit
    while (restOfValue > 0x80) {
        buffer.writeByte(restOfValue % 0x80 - 0x80)
        restOfValue /= 0x80
    }
    // write most significant bits, preceded by '0' bit
    buffer.writeByte(restOfValue)
}

internal fun encodeInsert(staticNameIndex: Int, dynamicNameIndex: Int, header: Header) {
    TODO("Not yet implemented")
}

internal fun encodeDynamicIndexReference(dynamicIndex: Int, base: Int) {
    TODO("Not yet implemented")
}

internal fun encodeLiteral(staticNameIndex: Int, line: Header) {
    TODO("Not yet implemented")
}

internal fun encodeDynamicLiteral(dynamicNameIndex: Int, base: Int, header: Header) {
    TODO("Not yet implemented")
}

internal fun encodeStaticIndexReference(buffer: Buffer, staticIndex: Int) {
    TODO("Not yet implemented")
}

enum class AcknowledgementMode {
    None,
    Immediate,
}
