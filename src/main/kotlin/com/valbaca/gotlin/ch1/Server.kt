package com.valbaca.gotlin.ch1

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/*
p19 HTTP Server, using com.sun.net.httpserver.HttpServer (wrapped by http4k as "SunHttp")
https://github.com/adonovan/gopl.io/blob/master/ch1/server1/main.go
 */
private const val PORT = 8000
fun main() {
    val app = { request: Request ->
        Response(OK).body("URL.Path = ${request.uri.path}")
    }
    println("Running at http://localhost:$PORT")
    app.asServer(SunHttp(PORT)).start()
}