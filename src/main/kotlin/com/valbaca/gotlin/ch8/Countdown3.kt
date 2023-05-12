package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

private fun launchRocket() {
    println("🌍 🚀 🌔")
}

suspend fun main() = coroutineScope {
    val abort = Channel<Any>()
    launch {
        System.`in`.read(ByteArray(1)) // read a single byte
        abort.send(object {})
    }
    println("Commencing countdown. Press return to abort.")
    val mainScope = this
    val tick = ticker(1000L)
    for (countdown in 10 downTo 1) {
        println(countdown)
        select {
            tick.onReceive {} // do nothing
            abort.onReceive {
                error("Launch aborted!")
            }
        }
    }
    launchRocket()
}