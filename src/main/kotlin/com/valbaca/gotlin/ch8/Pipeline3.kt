package com.valbaca.gotlin.ch8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
 p231
 https://github.com/adonovan/gopl.io/blob/master/ch8/pipeline3/main.go
 */
suspend fun counter(out: SendChannel<Int>) {
    for (x in 0 until 100) {
        out.send(x)
    }
    out.close()
}

suspend fun squarer(out: SendChannel<Int>, input: ReceiveChannel<Int>) {
    for (v in input) {
        out.send(v * v)
    }
    out.close()
}

suspend fun printer(input: ReceiveChannel<Int>) {
    for (v in input) {
        println(v)
    }
}


fun main() = runBlocking(Dispatchers.IO) {
    val naturals = Channel<Int>()
    val squares = Channel<Int>()
    launch { counter(naturals) }
    launch { squarer(squares, naturals) }
    printer(squares)
}