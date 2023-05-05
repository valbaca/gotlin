package com.valbaca.gotlin.ch1

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import java.time.Duration.between
import java.time.Instant.now

/* p17 Fetch requests, concurrently https://github.com/adonovan/gopl.io/blob/master/ch1/fetchall/main.go */
suspend fun main(args: Array<String>) {
    val urls = args.ifEmpty {
        arrayOf("https://www.google.com", "https://go.dev", "https://kotlinlang.org/", "https://gopl.io", "https://www.http4k.org/")
    }
    val start = now()
    val client = HttpClient(CIO)
    val ch = Channel<String>()
    runBlocking(Dispatchers.Default) {
        launch {
            for (url in urls) {
                fetch(client, url, ch)
            }
        }.invokeOnCompletion {
            ch.close()
        }

        ch.consumeEach { println(it) }
    }
    println("${between(start, now()).toMillis()}ms elapsed")
}

fun CoroutineScope.fetch(client: HttpClient, url: String, ch: Channel<String>) = launch {
    val start = now()
    try {
        val response = client.get(url)
        val n = response.body<ByteArray>().size
        ch.send("${between(start, now()).toMillis()}ms ${n}bytes $url")
    } catch (e: Exception) {
        ch.send("${between(start, now()).toMillis()}ms Exception ${e.message}")
    }
}