package com.valbaca.gotlin.ch1

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

fun main(args: Array<String>) {
//    println(args.joinToString(" "))
//    duplicateLines()
    findDuplicateLines(args)
}


/* p9 https://github.com/adonovan/gopl.io/blob/master/ch1/dup1/main.go */
fun duplicateLines() {

    val counts = mutableMapOf<String, Int>().withDefault { 0 }

    var input = Scanner(System.`in`)
    while (input.hasNext()) {
        val word = input.next()
        counts[word] = counts.getValue(word) + 1
    }

    for ((line, n) in counts) {
        if (n > 1) {
            println("${n}\t${line}")
        }
    }
}

/* p11 https://github.com/adonovan/gopl.io/blob/master/ch1/dup2/main.go */
fun findDuplicateLines(args: Array<String>) {
    val counts = mutableMapOf<String, Int>().withDefault { 0 }
    if (args.isEmpty()) {
        countLines(System.`in`, counts)
    } else {
        for (arg in args) {
            FileInputStream(File(arg)).use {
                countLines(it, counts)
            }
        }
    }

    for ((line, n) in counts) {
        if (n > 1) {
            println("${n}\t${line}")
        }
    }
}

fun countLines(f: InputStream, counts: MutableMap<String, Int>) {
    val input = Scanner(f)
    while (input.hasNext()) {
        val word = input.next()
        counts[word] = counts.getValue(word) + 1
    }
}
