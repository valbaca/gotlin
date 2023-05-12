package com.valbaca.gotlin.ch5

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachHref
import it.skrape.selects.html5.a
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking


suspend fun main() = runBlocking(Dispatchers.Default) {
    coroutineScope {
        val links = extractLinks("http://valbaca.com")
        links.forEach { println(it) }
    }
}

/**
 * Given a web-page's `url`, returns a deduped-list of all the urls on the page.
 * (Any relative links are turned into absolute urls)
 */
suspend fun extractLinks(url: String): List<String> {
    val links = skrape(BrowserFetcher) {
        request { this.url = url }
        response { htmlDocument { a { findAll { eachHref } } } }
    }
    return links.map { if (it.startsWith("/")) url + it else it }.toSet().toList()
}