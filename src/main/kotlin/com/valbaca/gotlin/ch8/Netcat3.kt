package com.valbaca.gotlin.ch8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Socket

/*
p227 Netcat3
https://github.com/adonovan/gopl.io/blob/master/ch8/netcat3/netcat.go
*/
fun main() = runBlocking(Dispatchers.IO) {
    val conn = Socket("localhost", 8000)
    val done = Channel<Any>() // Any is the top-level type in Kotlin
    launch {
        mustCopy(System.out.writer(), conn.getInputStream().reader())
        println("done")
        done.send(object {}) // send an empty object
    }
    mustCopy(conn.getOutputStream().writer(), System.`in`.reader())
    conn.close()
    done.receive()
    return@runBlocking /* [1] */
}

/* [1]: This is a weird quirk of Kotlin. By doing a `done.receive()` on the last line, I effectively
        changed the signature of the main function, which conflicts with the other main() functions
        with in this same package com.valbaca.gotlin.ch8 and breaks the build.

        So to workaround, I just add an explicit Unit return at the end */
