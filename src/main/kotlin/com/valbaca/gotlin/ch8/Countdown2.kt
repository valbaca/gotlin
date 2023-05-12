package com.valbaca.gotlin.ch8

import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

private fun launchRocket() {
    println("🌍 🚀 🌔")
}

/*
TIP: When writing `select`, to help the IDE, might start with `select<Unit>` and then remove the `<Unit>` once you're done with the select block.
 */
suspend fun main() = coroutineScope {
    val abort = Channel<Any>()
    launch {
        System.`in`.read(ByteArray(1)) // read a single byte
        abort.send(object {})
    }

    println("Commencing countdown. Press return to abort.")
    val parent = this.coroutineContext
    select {
        ticker(delayMillis = 0, initialDelayMillis = 10 * 1000L).onReceive {
            // do nothing
        }
        abort.onReceive {
            // FIXME: instead of error, ought to `return` out of `main` from here
            // I couldn't figure it out (or perhaps throwing is just correct for Kotlin?)
            error("Launch aborted!")
        }
    }
    launchRocket()
}