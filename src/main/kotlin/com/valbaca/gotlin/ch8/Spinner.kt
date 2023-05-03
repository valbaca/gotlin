package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/*
 * p218
 * https://github.com/adonovan/gopl.io/blob/master/ch8/spinner/main.go
 *
 * This is different because coroutines aren't automatically cancelled on exit
 * Goroutines do exit on main() end. Coroutines don't.
 *
 * So we keep track of the Job for the launched coroutine and `cancel()` the job.
 */
fun main() = runBlocking(Dispatchers.Default) {
    val spinnerJob: Job = spinner(200.toDuration(MILLISECONDS))
    val n = 45
    val fibN = fib(n) // slow
    println("\rFibonacci($n) = $fibN")
    spinnerJob.cancel() /* Have to manually cancel coroutines or it will keep spinning */
}

fun CoroutineScope.spinner(toDelay: Duration) = launch {
    while (isActive) {
        for (r in "-\\|/") {
            print("\r$r")
            delay(toDelay)
        }
    }
}

fun fib(x: Int): Int = if (x < 2) x else fib(x - 1) + fib(x - 2)