package com.valbaca.gotlin.ch8

import java.io.Reader
import java.io.Writer
import java.net.Socket

/*
p221 Netcat1
https://github.com/adonovan/gopl.io/tree/master/ch8/netcat1
*/
fun main() {
    val conn = Socket("localhost", 8000)
    mustCopy(System.out.bufferedWriter(), conn.getInputStream().bufferedReader())
}

fun mustCopy(dst: Writer, src: Reader) {
    src.writeTo(dst)
}