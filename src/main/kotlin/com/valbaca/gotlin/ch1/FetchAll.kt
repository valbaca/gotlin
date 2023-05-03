package com.valbaca.gotlin.ch1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import java.time.Duration.between
import java.time.Instant.now

/* p17 Fetch requests, concurrently https://github.com/adonovan/gopl.io/blob/master/ch1/fetchall/main.go */
fun main(args: Array<String>) {
    val urls = if (args.isNotEmpty()) {
        args
    } else {
        arrayOf("https://www.google.com", "https://go.dev", "https://kotlinlang.org/", "https://gopl.io", "https://www.http4k.org/")
    }
    val start = now()
    val ch = Channel<String>()
    runBlocking(Dispatchers.Default) {
        for (url in urls) {
            fetch(url, ch)
        }
        repeat(urls.size) {
            println(ch.receive())
        }
    }
    println("${between(start, now()).toMillis()}ms elapsed")
}

fun CoroutineScope.fetch(url: String, ch: Channel<String>) = launch {
    val start = now()
    try {
        val http = ApacheClient()
        val req = Request(Method.GET, url)
        val res = http(req)
        val n = res.body.length!!
        ch.send("${between(start, now()).toMillis()}ms $n $url")
    } catch (e: Exception) {
        ch.send("${between(start, now()).toMillis()}ms Exception ${e.message}")
    }
}