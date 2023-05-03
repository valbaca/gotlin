package com.valbaca.gotlin.ch8

import java.io.Reader
import java.io.Writer


/**
 * Writes contents of `this` to `out` until EOF.
 *
 * Replicates Go's io.Copy functionality and fixes InputStream.copyTo by adding flush()
 */
fun Reader.writeTo(out: Writer, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var charsCopied: Long = 0
    val buffer = CharArray(bufferSize)
    var n = read(buffer)
    while (n >= 0) {
        out.write(buffer, 0, n)
        charsCopied += n
        n = read(buffer)
        out.flush() // InputStream.copyTo is missing this!
    }
    return charsCopied
}