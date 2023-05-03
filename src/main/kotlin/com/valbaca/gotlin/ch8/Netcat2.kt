package com.valbaca.gotlin.ch8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Socket

/*
p223 Netcat2
https://github.com/adonovan/gopl.io/tree/master/ch8/netcat2
*/
fun main() = runBlocking(Dispatchers.IO) {
    val conn = Socket("localhost", 8000)
    onShutdown { conn.close() }
    launch {
        mustCopy(System.out.writer(), conn.getInputStream().reader())
    }
    mustCopy(conn.getOutputStream().writer(), System.`in`.reader())
}


