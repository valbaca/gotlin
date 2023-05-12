package com.valbaca.gotlin.ch8

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.whileSelect

typealias Client = SendChannel<String>

/* FYI, for a better chat example of Kotlin + Ktor, see https://github.com/ktorio/ktor-samples/tree/main/chat */
fun main() = runBlocking {
    launch { broadcaster() }
    // Ktor+TCP c/o https://ktor.io/docs/servers-raw-sockets.html#server-example
    val serverSocket = aSocket(SelectorManager(Dispatchers.IO)).tcp().bind("127.0.0.1", 8000)
    println("Server is listening at ${serverSocket.localAddress}")
    while (true) {
        val socket = serverSocket.accept()
        println("Accepted $socket")
        launch { handleConn(socket) }
    }
}

val entering = Channel<Client>()
val leaving = Channel<Client>()
val messages = Channel<String>()

suspend fun broadcaster() {
    val clients = mutableSetOf<Client>() // all connected clients
    whileSelect {
        messages.onReceive { msg ->
            // Broadcast incoming message to all clients' outgoing message channels
            clients.forEach { it.send(msg) }
            true
        }
        entering.onReceive { cli -> clients.add(cli); true }
        leaving.onReceive { cli ->
            clients.remove(cli)
            cli.close()
            true
        }
    }
}

suspend fun clientWriter(socket: Socket, ch: ReceiveChannel<String>) {
    val writeChannel = socket.openWriteChannel(autoFlush = true)
    writeChannel.use {
        ch.consumeEach { msg ->
            writeStringUtf8(msg.withNewline())
        }
    }
}

suspend fun handleConn(socket: Socket) = coroutineScope {
    socket.use {
        val ch = Channel<String>() // outgoing client messages
        launch { clientWriter(socket, ch) }
        val who = socket.remoteAddress.toString()
        ch.send("You are $who")
        messages.send("$who has arrived")
        entering.send(ch)

        val input = socket.openReadChannel()
        while (!input.isClosedForRead) {
            val msg = input.readUTF8Line()
            if (!msg.isNullOrBlank()) {
                messages.send("$who : $msg")
            }
        }
        leaving.send(ch)
        messages.send("$who has left")
    }
}