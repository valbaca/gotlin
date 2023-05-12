package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.ticker

private fun launchRocket() {
    println("🌍 🚀 🌔")
}

suspend fun main() {
    println("Commencing countdown.")
    val tick = ticker(1000L)
    for (countdown in 10 downTo 1) {
        println(countdown)
        tick.receive()
    }
    launchRocket()
}