package de.marcreichelt.qpack.qif

import de.marcreichelt.qpack.Header

/**
 * Parses QIF files (QPACK interop format), which are useful to test QPACK implementations.
 *
 * See [QPACK-Offline-Interop](https://github.com/quicwg/base-drafts/wiki/QPACK-Offline-Interop).
 */
fun parseQIF(qif: String): List<List<Header>> {
    val headerSets = mutableListOf<List<Header>>()
    var currentHeaderSet = mutableListOf<Header>()

    val anyWhitespace = Regex("\\s+")
    qif.lineSequence()
        .filterNot { it.startsWith("#") }
        .forEach { line ->
            if (line.isBlank()) {
                if (currentHeaderSet.isNotEmpty()) {
                    headerSets.add(currentHeaderSet.toList()) // add current header set
                    currentHeaderSet = mutableListOf() // start next header set
                }
            } else {
                val (key, value) = line.split(anyWhitespace, 2)
                currentHeaderSet.add(Header(key, value))
            }
        }

    // add last header set
    if (currentHeaderSet.isNotEmpty()) {
        headerSets.add(currentHeaderSet.toList())
    }

    return headerSets.toList()
}
