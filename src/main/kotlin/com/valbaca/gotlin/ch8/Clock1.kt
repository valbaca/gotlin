package com.valbaca.gotlin.ch8

import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/*
p220 Blocking TCP Clock Server
https://github.com/adonovan/gopl.io/blob/master/ch8/clock1/clock.go
*/
private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
fun main() {
    val listener = ServerSocket(8000)
    while (true) {
        try {
            val conn = listener.accept()!!
            handleConn(conn)
        } catch (e: Exception) {
            error(e)
        }
    }
}

private fun handleConn(conn: Socket) {
    while (true) {
        val out = conn.getOutputStream()
        val formattedTime = clockFormatter.format(LocalTime.now()) + "\n"
        out.write(formattedTime.toByteArray())
        Thread.sleep(1.toDuration(SECONDS).inWholeMilliseconds) // Notice this is `sleep` for the thread model
    }
}
