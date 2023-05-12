package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.supervisorScope
import java.io.File

suspend fun walkDirSema(dir: File, fileSizes: Channel<Long>) {
    for (entry in direntsSema(dir)) {
        if (entry.isDirectory) {
            walkDirSema(entry, fileSizes)
        }
        fileSizes.send(entry.length())
    }
}

private val sema = Channel<Unit>(20)

suspend fun direntsSema(dir: File): List<File> {
    sema.send(Unit)
    return try {
        dir.listFiles()?.toList() ?: emptyList()
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
    val fileSizes = Channel<Long>()
    launch {
        // supervisor scope plays the equivalent role as sync.WaitGroup
        supervisorScope {
            for (root in roots) {
                launch { walkDirSema(root.toFile(), fileSizes) }
            }
        }
        fileSizes.close()
    }
    // Print result periodically
    val tick = if (verbose) ticker(500L) else Channel()
    var nfiles = 0L
    var nbytes = 0L
    whileSelect {
        fileSizes.onReceiveCatching {
            if (it.isSuccess) {
                nfiles++
                nbytes += it.getOrThrow()
                true
            } else {
                false
            }
        }
        tick.onReceive {
            printDiskUsage(nfiles, nbytes)
            true
        }
    }
    printDiskUsage(nfiles, nbytes)
}

private fun printDiskUsage(nfiles: Long, nbytes: Long) {
    println("$nfiles files ${"%.1f".format(nbytes.toFloat() / 1e6)}MB ")
}