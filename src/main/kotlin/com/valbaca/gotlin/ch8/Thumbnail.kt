package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import java.io.File
import java.io.IOException
import java.lang.Thread.sleep
import java.time.Duration
import java.time.Instant.now
import kotlin.time.Duration.Companion.milliseconds

/*
p235
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go
 */
suspend fun main() {
    val files = listOf("profile.jpg", "cats.jpg", "meme.jpg")
    var start = now()

    makeThumbnails2(files)
    println("Took: ${Duration.between(start, now()).toMillis()}ms\n")

    start = now()
    makeThumbnails3(files)
    println("Took: ${Duration.between(start, now()).toMillis()}ms\n")

    start = now()
    makeThumbnails4(files)
//        makeThumbnails4((files + "error") + "home.jpg")
    println("Took: ${Duration.between(start, now()).toMillis()}ms\n")

    start = now()
    makeThumbnails5(files)
//        makeThumbnails5((files + "error") + "home.jpg")
    println("Took: ${Duration.between(start, now()).toMillis()}ms\n")

    start = now()
    val ch = Channel<String>(files.size).apply { files.forEach { send(it) } }
    ch.close()
    makeThumbnails6(ch)

    println("Took: ${Duration.between(start, now()).toMillis()}ms")
}

/*
Using GlobalScope launches a task that is not attached the current scope of execution.
This method will return, but the imageFile operation can continue to execute.

Additionally, the try/catch here does not do what you might expect. It will not trap exceptions
from imageFile because the block passed to `launch` is executed concurrently.

https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L30
 */
fun makeThumbnails2(filenames: List<String>) {
    for (f in filenames) {
        try {
            GlobalScope.launch { imageFile(f) } // Unlike go, we don't have to worry about loop variable capture inside launch block
        } catch (e: IOException) {
            println(e)
        }
    }
    println("End of makeThumbnails2")
}

/*
See above for why this examples doesn't even make much sense
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L41

The `coroutineScope` block within this method does not exit until all the `launch` jobs
complete. If any one of the launch jobs throws an exception, the scope will be cancelled, all
other jobs still in progress will be cancelled and the exception is propagated.
 */
suspend fun makeThumbnails3(filenames: List<String>) = coroutineScope {
    for (f in filenames) {
        launch {
            imageFile(f)            // Unlike go, we don't have to worry about loop variable capture inside launch block
        }
    }
    println("End of makeThumbnails3")
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L61

This operation uses supervisorScope, which will prevent a failing launch job from cancelling the
scope. This method will exit normally once all the launch jobs complete, even if one or more
of them throw an exception.
 */
suspend fun makeThumbnails4(filenames: List<String>) = supervisorScope {
    filenames.forEach { launch { imageFile(it) } }
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L86

Unlike `launch`, which is a fire-and-forget job, `async` will return the result of the imageFile
method. The `awaitAll` method will wait for all the jobs to complete and return a list of
the results. The first async job that throws an exception will cause any other jobs in progress
to be cancelled and the exception will be propagated.
 */
suspend fun makeThumbnails5(filenames: List<String>): List<String> = coroutineScope {
    filenames.map { async { imageFile(it) } }.awaitAll()
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L117
 */
suspend fun makeThumbnails6(filenames: ReceiveChannel<String>): Long = coroutineScope {
    buildList {
        filenames.consumeEach {
            add(async { stat(imageFile(it)) })
        }
    }.awaitAll().sum()

    // Alternatively, with a flow
//    filenames.consumeAsFlow().map { stat(imageFile(it)) }.toList().sum()
}

/**
 * ImageFile PRETENDS to read an image from infile and PRETENDS to write a thumbnail-size version in the same directory.
 *
 * It returns a generated file name, e.g. "foo.thumb.jpg"
 *
 * This is a PRETEND function because the goal is NOT to get into the actual aspects of image or file writing.
 * The point is to have an example function that demonstrates a function that does a mix of blocking and non-blocking.
 *
 * @throws IOException if the output file cannot be written
 */
private val waitTimeMs = 500L
private val actuallyCreate = false

suspend fun imageFile(infile: String): String {
    println("Starting: $infile")
    repeat(2) {
        delay(waitTimeMs.milliseconds) // non-blocking delay (like NIO file access)
        sleep(waitTimeMs)              // blocking delay (CPU work of resizing file)
    }
    val newFileName =
        infile.substringBeforeLast('.') + ".thumb." + infile.substringAfterLast('.', missingDelimiterValue = "")
    if (actuallyCreate) {
        val file = File(newFileName).createNewFile()
        if (!file) {
            throw IOException("File $newFileName already exists")
        }
    }
    if (infile == "error") {
        throw IOException("Forced error")
    }
    println("Finished: $newFileName")
    return newFileName
}

/** Another fake function */
fun stat(file: String): Long = file.length.toLong() * 1024L