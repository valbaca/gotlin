package com.valbaca.gotlin.ch8

import com.valbaca.gotlin.ch5.extractLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val tokens = Channel<Any>(20)

private suspend fun crawl(url: String): List<String> {
    println(url)
    tokens.send(object {}) // acquire a token
    return try {
        extractLinks(url)
    } catch (e: Exception) {
        error(e)
    } finally {
        tokens.receive() // release the token
    }
}

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    val worklist = Channel<List<String>>()

    val start = listOf(args.ifEmpty { arrayOf("https://news.ycombinator.com/") }[0])
    launch { worklist.send(start) }
    var n = 1 // n = # of pending sends to worklist

    // Crawl the web concurrently (with concurrency bounds)
    // ⚠️ WARNING! CRAWLER WILL LIKELY RUN FOREVER! ⚠️️
    // TIP: add depth-limits
    val seen = mutableSetOf<String>()
    while (n > 0) {
        n--
        val list = worklist.receive()
        for (link in list) {
            if (link !in seen) {
                seen.add(link)
                n++
                launch { worklist.send(crawl(link)) }
            }
        }
    }
}