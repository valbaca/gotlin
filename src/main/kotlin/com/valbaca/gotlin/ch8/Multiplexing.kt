package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select

/*
This is from the example on p245 (couldn't find the example within the Go github repo).
It's supposed to print out: 0 2 4 6 8
The buffer size is 1, so the loop goes:
 - i := 0 => send 0 to channel
 - i := 1 => receive 0 from channel and print 0
 - i := 2 => send 2 to channel
 - i := 3 => receive 2 from channel and print 2
 etc. until it prints out all even numbers in the range.

Kotlin's select is different from Go.

1. IF multiple cases are ready, Go will pick at random BUT Kotlin's `select` biases toward the first case. Use `selectUnbiased` for Go-like behavior

2. More importantly, Kotlin doesn't allow multiple select clauses on the same object:
    java.lang.IllegalStateException: Cannot use select clauses on the same object

So the Go example, which prints even numbers out, simply doesn't work in Kotlin.
 */
suspend fun main() {
    val ch = Channel<Int>(1)
    for (i in 0 until 10) {
        select {
            ch.onReceive { x -> println(x) }
            ch.onSend(i) {}
        }
    }
}