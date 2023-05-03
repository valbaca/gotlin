package com.valbaca.gotlin.ch8

import java.io.BufferedReader
import java.io.PrintStream
import java.net.Socket

/*
p221 Netcat
https://github.com/adonovan/gopl.io/tree/master/ch8/netcat1
*/
fun main() {
    val conn = Socket("localhost", 8000)
    mustCopy(System.out, conn.getInputStream().bufferedReader())
}

fun mustCopy(dst: PrintStream, src: BufferedReader) {
    src.use {
        while (true) {
            try {
                dst.println(it.readLine())
            } catch (e: Exception) {
                error(e)
            }
        }
    }
}
