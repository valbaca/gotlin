package com.valbaca.gotlin.ch1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/*
p20
https://github.com/adonovan/gopl.io/blob/master/ch1/server2/main.go
https://github.com/adonovan/gopl.io/blob/master/ch1/server3/main.go
*/
private const val PORT = 8000
var mutex = Mutex()
var count = 0

fun main() {
    val app: HttpHandler = routes(
        "/" bind GET to handler(), "/count" bind GET to counter(), "/echo" bind GET to echo()
    )
    println("Running at http://localhost:$PORT")
    app.asServer(SunHttp(PORT)).start()
}


fun handler() = { req: Request ->
    runBlocking(Dispatchers.Default) {
        mutex.withLock {
            count++
        }
    }
    Response(OK).body("URL.Path = ${req.uri.path}")
}

fun counter() = { _: Request ->
    runBlocking(Dispatchers.Default) {
        mutex.withLock {
            Response(OK).body("Count $count")
        }
    }
}

fun echo() = { req: Request ->
    val body = buildString {
        appendLine("${req.method} ${req.uri} ${req.version}")
        for ((k, v) in req.headers) {
            appendLine("Header[$k] = $v")
        }
        appendLine("Host = ${req.uri.host}")
        appendLine("RemoteAddr = ${req.source?.address}")
        for ((k, v) in req.form()) {
            appendLine("Form[$k] = $v")
        }
    }
    Response(OK).body(body)
}
