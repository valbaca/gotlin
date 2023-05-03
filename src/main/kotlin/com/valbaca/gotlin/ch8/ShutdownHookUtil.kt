package com.valbaca.gotlin.ch8

/**
 * Defer block to execute on shutdown.
 * Just a fancy wrapper around `Runtime.getRuntime().addShutdownHook`
 * Not at all what `defer` does in Go (which defers to end of block), but close enough
 */
fun onShutdown(block: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            block()
        }
    })
}