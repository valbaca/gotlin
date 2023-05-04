package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

/*
p222 NonBlocking TCP Clock Server using coroutines
https://github.com/adonovan/gopl.io/tree/master/ch8/clock2
*/
private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val printUncaughtException = CoroutineExceptionHandler { _, ex -> ex.printStackTrace() }

suspend fun main() = supervisorScope {
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

private fun CoroutineScope.handleConn(conn: Socket) = launch(printUncaughtException) {
    conn.use {
        while (isActive) { // `isActive` instead of `true` is a good pattern
            val out = conn.getOutputStream()
            val formattedTime = clockFormatter.format(LocalTime.now()) + "\n"
            print(".")
            out.write(formattedTime.toByteArray())
            out.flush()
            delay(1.seconds) // Notice this is `delay` for the coroutine model
        }
    }
}
