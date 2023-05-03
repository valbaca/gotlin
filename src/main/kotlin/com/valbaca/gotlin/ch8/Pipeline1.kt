package com.valbaca.gotlin.ch8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
/*
 p228
 https://github.com/adonovan/gopl.io/blob/master/ch8/pipeline1/main.go
 */
fun main() = runBlocking(Dispatchers.IO) {
    val naturals = Channel<Int>()
    val squares = Channel<Int>()

    launch {
        for (x in 0..Int.MAX_VALUE) {
            naturals.send(x)
        }
    }
    launch {
        while (isActive) {
            val x = naturals.receive()
            squares.send(x * x)
        }
    }
    // Printer in main
    while (true) {
        println(squares.receive())
    }
}