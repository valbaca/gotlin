package com.valbaca.gotlin.ch8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
 p229
 https://github.com/adonovan/gopl.io/blob/master/ch8/pipeline2/main.go
 */
fun main() = runBlocking(Dispatchers.IO) {
    val naturals = Channel<Int>()
    val squares = Channel<Int>()

    launch {
        for (x in 0 until 100) {
            naturals.send(x)
        }
        naturals.close()
    }
    launch {
        for (x in naturals) {
            squares.send(x * x)
        }
    }
    // Printer in main
    for (x in squares) {
        println(x)
    }
}