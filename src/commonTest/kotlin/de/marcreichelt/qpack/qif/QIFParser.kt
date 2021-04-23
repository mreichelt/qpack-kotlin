package de.marcreichelt.qpack.qif

data class HeaderLine(val key: String, val value: String) {
    override fun toString(): String = "$key $value"
}

/**
 * Parses QIF files (QPACK interop format), which are useful to test QPACK implementations.
 *
 * See [QPACK-Offline-Interop](https://github.com/quicwg/base-drafts/wiki/QPACK-Offline-Interop).
 */
fun parseQIF(qif: String): List<List<HeaderLine>> {
    val headerSets = mutableListOf<List<HeaderLine>>()
    var currentHeaderSet = mutableListOf<HeaderLine>()

    qif.lineSequence()
        .filterNot { it.startsWith("#") }
        .forEach { line ->
            if (line.isBlank()) {
                if (currentHeaderSet.isNotEmpty()) {
                    headerSets.add(currentHeaderSet.toList()) // add current header set
                    currentHeaderSet = mutableListOf() // start next header set
                }
            } else {
                val (key, value) = line.split(Regex(" +"), 2)
                currentHeaderSet.add(HeaderLine(key, value))
            }
        }

    // add last header set
    if (currentHeaderSet.isNotEmpty()) {
        headerSets.add(currentHeaderSet.toList())
    }

    return headerSets.toList()
}
