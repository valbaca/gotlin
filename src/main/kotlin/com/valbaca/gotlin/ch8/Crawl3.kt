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
    val worklist = Channel<List<String>>() // lists of URLs, may have duplicates
    val unseenLinks = Channel<String>()    // de-duplicated URLs

    val start = listOf(args.ifEmpty { arrayOf("https://news.ycombinator.com/") }[0])
    launch { worklist.send(start) }

    repeat(20) {
        launch {
            unseenLinks.consumeEach { link ->
                val foundLinks = crawl(link)
                launch {
                    worklist.send(foundLinks)
                }
            }
        }
    }

    // The main coroutine de-duplicates worklist items
    // and sends the unseen ones to the crawlers
    val seen = mutableSetOf<String>()
    worklist.consumeEach { list ->
        for (link in list) {
            if (link !in seen) {
                seen.add(link)
                unseenLinks.send(link)
            }
        }
    }
    // For simplicity (and reflecting The Go book, does not address the termination problem)
}