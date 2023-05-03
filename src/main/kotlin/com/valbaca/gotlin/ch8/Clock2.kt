package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/*
p222 NonBlocking TCP Clock Server using coroutines
https://github.com/adonovan/gopl.io/tree/master/ch8/clock2
*/
private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
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
        while (isActive) { // `isActive` instead of `true` is a good pattern
            val out = conn.getOutputStream()
            val formattedTime = clockFormatter.format(LocalTime.now()) + "\n"
            print(".")
            try {
                out.write(formattedTime.toByteArray())
                out.flush()
            } catch (e: Exception) {
                cancel()
                System.err.print(e)
            }
            delay(1.toDuration(SECONDS)) // Notice this is `delay` for the coroutine model
        }
    }

}
