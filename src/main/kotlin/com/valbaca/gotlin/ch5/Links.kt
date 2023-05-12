package com.valbaca.gotlin.ch5

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.eachHref
import it.skrape.selects.html5.a
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking


suspend fun main() = runBlocking(Dispatchers.Default) {
    coroutineScope {
        val links = extractLinks("https://news.ycombinator.com/")
        links.forEach { println(it) }
    }
}

/**
 * Given a web-page's `url`, returns a deduped-list of all the urls on the page.
 * (Any relative links are turned into absolute urls)
 */
suspend fun extractLinks(url: String): List<String> {
    // FIXME: skrape does have an AsyncFetcher but it throws ClassNotFoundException due to conflicts with ktor
    val links = try {
        skrape(BrowserFetcher) {
            request { this.url = url }
            response { htmlDocument { a { findAll { eachHref } } } }
        }
    } catch (e: ElementNotFoundException) { // catch if pages has no links
        emptyList()
    }
    return links.map { if (it.startsWith("/")) url + it else it }.toSet().toList()
}