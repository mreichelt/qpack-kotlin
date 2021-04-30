package de.marcreichelt.qpack

import de.marcreichelt.qpack.Qpack.staticTable
import kotlin.test.Test
import kotlin.test.assertEquals


class QpackTest {

    @Test
    fun checkStaticTable() {
        assertEquals(staticTable.size, 99) // qpack v21

        assertEquals(Header(":authority", ""), staticTable[0])
        assertEquals(Header(":status", "404"), staticTable[27])
        assertEquals(
            Header("content-security-policy", "script-src 'none'; object-src 'none'; base-uri 'none'"),
            staticTable[85]
        )
        assertEquals(Header("x-frame-options", "sameorigin"), staticTable[98])
    }

}
