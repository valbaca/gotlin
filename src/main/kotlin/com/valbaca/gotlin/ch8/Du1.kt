package com.valbaca.gotlin.ch8

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    val roots = args.toList().ifEmpty { listOf(".") }
    val fileSizes = Channel<Long>()
    launch {
        for (root in roots) {
            walkDir(root.toFile(), fileSizes)
        }
        fileSizes.close()
    }
    var nfiles = 0L
    var nbytes = 0L
    fileSizes.consumeEach { size ->
        nfiles++
        nbytes += size
    }
    println("$nfiles files ${"%.1f".format(nbytes.toFloat()/1e6)}MB ")
}