package com.valbaca.gotlin.ch8

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
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
fun main() {
    val files = listOf("profile.jpg", "cats.jpg", "meme.jpg")
    var start = now()
    runBlocking(Dispatchers.Default) {
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
    }
    println("Took: ${Duration.between(start, now()).toMillis()}ms")
}

/*
The runBlocking does (as the name implies) block until coroutines are complete.

This is unlike Go and obviates the example in the book, which is meant to show that Go will exit the main function
even if there are open/active goroutines.
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L30
 */
suspend fun makeThumbnails2(filenames: List<String>) = coroutineScope {
    for (f in filenames) {
        try {
            launch { imageFile(f) } // Unlike go, we don't have to worry about loop variable capture inside launch block
        } catch (e: IOException) {
            println(e)
        }
    }
    println("End of makeThumbnails2")
}

/*
See above for why this examples doesn't even make much sense
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L41
 */
suspend fun makeThumbnails3(filenames: List<String>) = coroutineScope {
    val ch = Channel<Any>()
    for (f in filenames) {
        launch {
            imageFile(f)            // Unlike go, we don't have to worry about loop variable capture inside launch block
            ch.send(object {})
        }
    }
    println("End of for loop")
    repeat(filenames.size) {
        ch.receive()
    }
    println("End of makeThumbnails3")
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L61
 */
suspend fun makeThumbnails4(filenames: List<String>) = coroutineScope {
    val exceptions = Channel<Exception?>()
    for (f in filenames) {
        launch {
            try {
                imageFile(f)
                exceptions.send(null)
            } catch (e: Exception) {
                exceptions.send(e)
            }
        }
    }
    repeat(filenames.size) {
        val e = exceptions.receive()
        if (e != null) {
            throw e // Unlike go, this will NOT result in a coroutine leak. Per the documentation of `coroutineScope`
            // However, if we used supervisorScope, we would need to protect against this. See makeThumbnails5
            /* When any child coroutine in this scope fails, this scope fails and all the rest of the children are
             cancelled (for a different behavior see supervisorScope). */
        }
    }
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L86
 */
suspend fun makeThumbnails5(filenames: List<String>): List<String> = supervisorScope {
    // TIP: Consider Arrow-kt's Either https://apidocs.arrow-kt.io/arrow-core/arrow.core/-either/index.html
    data class Item(val thumbfile: String?, val error: Exception?)


    val ch = Channel<Item>(filenames.size)
    for (f in filenames) {
        launch {
            // This imitates Go's multi-return functionality
            val item = try {
                Item(imageFile(f), null)
            } catch (e: Exception) {
                Item(null, e)
            }
            ch.send(item)
        }
    }
    buildList {
        repeat(filenames.size) {
            val item = ch.receive()
            if (item.error != null) {
                throw item.error
            }
            add(item.thumbfile!!)
        }
    }
}

/*
https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L117
 */
suspend fun makeThumbnails6(filenames: ReceiveChannel<String>): Long = coroutineScope {
    val sizes = Channel<Long>()
    val jobs = mutableListOf<Job>()
    // I *think* this is the Kotlin equivalent of a sync.WaitGroup. Please correct or confirm!
    // https://github.com/adonovan/gopl.io/blob/master/ch8/thumbnail/thumbnail_test.go#L119
    for (f in filenames) {
        val job = launch {
            try {
                val thumb = imageFile(f)
                sizes.send(stat(thumb))
            } catch (e: Exception) {
                throw e
            }
        }
        jobs.add(job)
    }
    launch {
        for (job in jobs) {
            job.join() // join on the jobs rather than use a WaitGroup
        }
        sizes.close()
    }
    var total = 0L
    for (size in sizes) {
        total += size
    }
    total
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