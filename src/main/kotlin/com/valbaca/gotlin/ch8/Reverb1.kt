package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/*
p223 Reverberating Echo Server
https://github.com/adonovan/gopl.io/blob/master/ch8/reverb1/reverb.go
*/
fun main() = runBlocking(Dispatchers.IO) {
    val listener = ServerSocket(8000)
    while (isActive) {
        try {
            val conn = listener.accept()!!
            handleConn(conn)
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
}

private fun CoroutineScope.handleConn(conn: Socket) = launch {
    conn.use {
        while (isActive) {
            val input = Scanner(conn.getInputStream().bufferedReader())
            while (input.hasNext()) {
                echo(conn, input.next(), 1.toDuration(SECONDS))
            }
        }
    }
}

fun echo(conn: Socket, shout: String, delayDuration: Duration) {
    with(conn.getOutputStream().writer()) {
        write("\t${shout.uppercase()}")
        flush()
        sleep(delayDuration.inWholeMilliseconds)
        write("\t$shout")
        flush()
        sleep(delayDuration.inWholeMilliseconds)
        write("\t${shout.lowercase()}")
        flush()
    }
}
