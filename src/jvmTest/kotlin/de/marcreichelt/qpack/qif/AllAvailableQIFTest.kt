package de.marcreichelt.qpack.qif

import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertTrue


class AllAvailableQIFTest {

    @Test
    fun parseAllAvailableQifFiles() {
        val resource: URL = object {}::class.java.getResource("/qifs/qifs")
        val qifsDirectory = File(resource.toURI())
        val qifFiles: List<File> = qifsDirectory.listFiles { _, name -> name.endsWith(".qif") }!!.toList()

        assertTrue(qifFiles.isNotEmpty())

        qifFiles.forEach { file ->
            println("parsing " + file.name)
            val qif = file.readText()

            val headerSets = parseQIF(qif)
            assertTrue(headerSets.isNotEmpty())
            assertTrue(headerSets.all { it.isNotEmpty() })
        }
    }

}