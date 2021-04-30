package de.marcreichelt.qpack

import de.marcreichelt.qpack.AcknowledgementMode.None
import de.marcreichelt.qpack.Header.Companion.RESPONSE_STATUS
import de.marcreichelt.qpack.Header.Companion.TARGET_AUTHORITY
import de.marcreichelt.qpack.Header.Companion.TARGET_METHOD
import de.marcreichelt.qpack.Header.Companion.TARGET_PATH
import de.marcreichelt.qpack.Header.Companion.TARGET_SCHEME
import okio.ByteString
import okio.ByteString.Companion.decodeHex

inline class QuicStreamId(val value: Int) {
    init {
        require(value >= 1)
    }
}

object Qpack {

    /**
     * Static table as defined in [QPACK](https://quicwg.org/base-drafts/draft-ietf-quic-qpack.html#section-appendix.a)
     */
    val staticTable = listOf(
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

    class Writer(
        val maxDynamicTableCapacity: Int = 0,
        val maxBlockedStreams: Int = 0,
        val acknowledgementMode: AcknowledgementMode = None,
    ) {

        fun encode(streamId: QuicStreamId, headers: List<Header>): ByteString {
            return "0000".decodeHex()
        }

    }

}

enum class AcknowledgementMode {
    None,
    Immediate,
}