package com.valbaca.gotlin.ch8

import kotlin.io.path.Path

fun String.toFile() = Path(this).toFile()

fun String.withNewline() = if (this.endsWith("\n")) this else this + "\n"
