package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.io.File
import kotlin.io.path.Path

private fun String.toFile() = Path(this).toFile()

private suspend fun walkDir(dir: File, fileSizes: Channel<Long>) {
    for (entry in dirents(dir)) {
        if (entry.isDirectory) {
            walkDir(entry, fileSizes)
        }
        fileSizes.send(entry.length())
    }
}

private fun dirents(dir: File): List<File> {
    return try {
        dir.listFiles()?.toList() ?: emptyList()
    } catch (e: Exception) {
        return emptyList()
    }
}

suspend fun main(args: Array<String>): Unit = coroutineScope {
    val argList = args.toList()
    val verbose = "-v" in argList
    val roots = argList.filter { it != "-v" }.ifEmpty { listOf(".") }
    val fileSizes = Channel<Long>()
    launch {
        for (root in roots) {
            walkDir(root.toFile(), fileSizes)
        }
        fileSizes.close()
    }
    // Print result periodically
    val tick = if (verbose) ticker(500L) else Channel()
    var nfiles = 0L
    var nbytes = 0L
    var stop = false
    while(!stop) {
        select {
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