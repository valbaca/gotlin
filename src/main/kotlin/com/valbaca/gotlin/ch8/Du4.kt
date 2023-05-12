package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
import java.io.File

val done = Channel<Unit>()

/*
Kotlin doesn't have a `default` in `select` so instead we use a blocking `when` with else
Also, Kotlin's Channels throw errors if they're read after they're closed.
Go's channels return zero values if they're read after close.
So to use a Kotlin channel as a broadcast, need to check `isClosedForReceive` (but even this is sketchy even though it works)
*/
fun cancelled() = when {
    done.isClosedForReceive -> true
    else -> false
}

suspend fun walkDirSemaCancellable(dir: File, fileSizes: Channel<Long>) {
    if (cancelled()) return
    for (entry in direntsSemaCancellable(dir)) {
        if (entry.isDirectory) {
            walkDirSemaCancellable(entry, fileSizes)
        }
        fileSizes.send(entry.length())
    }
}

private val sema = Channel<Unit>(20)

suspend fun direntsSemaCancellable(dir: File): List<File> {
    sema.send(Unit)
    return try {
        if (cancelled()) {
            emptyList()
        } else {
            dir.listFiles()?.toList() ?: emptyList()
        }
    } catch (e: Exception) {
        return emptyList()
    } finally {
        sema.receive()
    }
}

suspend fun main(args: Array<String>): Unit = coroutineScope {
    val argList = args.toList()
    val verbose = "-v" in argList
    val roots = argList.filter { it != "-v" }.ifEmpty { listOf(".") }

    // Cancel traversal when input is detected
    launch {
        System.`in`.read()
        done.close()
    }

    val fileSizes = Channel<Long>()
    launch {
        // supervisor scope plays the equivalent role as sync.WaitGroup
        supervisorScope {
            for (root in roots) {
                launch { walkDirSemaCancellable(root.toFile(), fileSizes) }
            }
        }
        fileSizes.close()
    }
    // Print result periodically
    val tick = if (verbose) ticker(500L) else Channel()
    var nfiles = 0L
    var nbytes = 0L
    var stop = false
    while (!stop) {
        select<Unit> {
            done.onReceive {
                // Drain fileSizes to allow existing coroutines to finish
                fileSizes.consumeEach { /* do nothing */ }
                stop = true
            }
            fileSizes.onReceiveCatching {
                if (it.isSuccess) {
                    nfiles++
                    nbytes += it.getOrThrow()
                } else {
                    stop = true
                }
            }
            tick.onReceive {
                printDiskUsage(nfiles, nbytes)
            }
        }
    }
    printDiskUsage(nfiles, nbytes)
}

private fun printDiskUsage(nfiles: Long, nbytes: Long) {
    println("$nfiles files ${"%.1f".format(nbytes.toFloat() / 1e6)}MB ")
}