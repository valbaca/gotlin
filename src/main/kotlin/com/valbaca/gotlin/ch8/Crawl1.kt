package com.valbaca.gotlin.ch8

import com.valbaca.gotlin.ch5.extractLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private suspend fun crawl(url: String): List<String> {
    println(url)
    return try {
        extractLinks(url)
    } catch (e: Exception) {
        error(e)
    }
}

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    val worklist = Channel<List<String>>()
    val start = listOf(args.ifEmpty { arrayOf("https://news.ycombinator.com/") }[0])
    launch { worklist.send(start) }

    // Crawl the web concurrently
    // 🚨 ⚠️ WARNING! CRAWLER HAS UNBOUNDED CONCURRENCY! 🚨 ⚠️
    // See Crawl2.kt
    val seen = mutableSetOf<String>()
    worklist.consumeEach {
        for (link in it) {
            if (link !in seen) {
                seen.add(link)
                launch {
                    worklist.send(crawl(link))
                }
            }
        }
    }
}