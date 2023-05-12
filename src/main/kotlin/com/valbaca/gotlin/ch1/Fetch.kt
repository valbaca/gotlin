package com.valbaca.gotlin.ch1

import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request

/* p16 Fetch requests, sequentially https://github.com/adonovan/gopl.io/blob/master/ch1/fetch/main.go */
fun main(args: Array<String>) {
    fetch(args)
}

fun fetch(args: Array<String>) {
    val http = ApacheClient()
    for (arg in args) {
        val req = Request(Method.GET, arg)
        println(http(req).body)
    }
}
