package com.valbaca.gotlin.ch8

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.Socket

/*
p227 Netcat3
https://github.com/adonovan/gopl.io/blob/master/ch8/netcat3/netcat.go
*/
suspend fun main(): Unit = coroutineScope {
    val conn = Socket("localhost", 8000)
    launch {
        mustCopy(System.out.writer(), conn.getInputStream().reader())
        println("done")
    }
    mustCopy(conn.getOutputStream().writer(), System.`in`.reader())
    conn.close()
}