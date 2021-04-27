/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.marcreichelt.qpack

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

/** HTTP header: the name is an ASCII string, but the value can be UTF-8. */
data class Header(
    /** Name in case-insensitive ASCII encoding. */
    val name: ByteString,
    /** Value in UTF-8 encoding. */
    val value: ByteString
) {

    // TODO: search for toLowerCase and consider moving logic here.
    constructor(name: String, value: String) : this(name.encodeUtf8(), value.encodeUtf8())

    constructor(name: ByteString, value: String) : this(name, value.encodeUtf8())

    override fun toString(): String = "${name.utf8()}: ${value.utf8()}"

    companion object {
        val RESPONSE_STATUS: ByteString = ":status".encodeUtf8()
        val TARGET_METHOD: ByteString = ":method".encodeUtf8()
        val TARGET_PATH: ByteString = ":path".encodeUtf8()
        val TARGET_SCHEME: ByteString = ":scheme".encodeUtf8()
        val TARGET_AUTHORITY: ByteString = ":authority".encodeUtf8()
    }
}
