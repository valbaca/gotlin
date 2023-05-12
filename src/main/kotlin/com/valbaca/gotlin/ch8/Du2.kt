package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


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
    while (!stop) {
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