package de.marcreichelt.qpack.qif

import de.marcreichelt.qpack.Header
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QIFParserTest {

    @Test
    fun parseSampleQIF() {
        val qif = """
            # I am a QIF file
            :method GET
            :scheme http
            :authority      www.netbsd.org
            :path   /
            user-agent      Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0
            accept  text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
            accept-language en-US,en;q=0.5
            accept-encoding gzip, deflate
            connection      keep-alive
            upgrade-insecure-requests       1
            pragma  no-cache
            cache-control   no-cache

            :method GET
            :scheme http
            :authority      www.netbsd.org
            :path   /global.css
            user-agent      Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0
            accept  text/css,*/*;q=0.1
            accept-language en-US,en;q=0.5
            accept-encoding gzip, deflate
            referer http://www.netbsd.org/
            connection      keep-alive
            pragma  no-cache
            cache-control   no-cache
        """.trimIndent()

        val headerSets = parseQIF(qif)
        assertEquals(headerSets.size, 2)
    }

    @Test
    fun parseEmptyQIF() {
        val headerSets = parseQIF("")
        assertTrue(headerSets.isEmpty())
    }

    @Test
    fun checkCompleteQIF() {
        val qif = """
            # I am a QIF file
            :method GET
            :scheme http
            :authority      www.netbsd.org
            :path   /
            connection      keep-alive

            :method GET
            :scheme http
            :authority      www.netbsd.org
            :path   /global.css
            user-agent      Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0
        """.trimIndent()
        val headerSets = parseQIF(qif)
        assertEquals(
            headerSets, listOf(
                listOf(
                    Header(":method", "GET"),
                    Header(":scheme", "http"),
                    Header(":authority", "www.netbsd.org"),
                    Header(":path", "/"),
                    Header("connection", "keep-alive"),
                ),
                listOf(
                    Header(":method", "GET"),
                    Header(":scheme", "http"),
                    Header(":authority", "www.netbsd.org"),
                    Header(":path", "/global.css"),
                    Header(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0"
                    ),
                ),
            )
        )
    }

}