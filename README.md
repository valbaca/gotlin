# gotlin

Structured concurrency
from [The Go Programming Language](https://www.gopl.io/) [code](https://github.com/adonovan/gopl.io/)
reimagined/reimplemented
in [Kotlin](https://kotlinlang.org/) [coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

`fun gotlin(go): kotlin`

**Why?**

Go's goroutines and Kotlin's coroutines have a lot in common (and only one letter different)!

While learning Kotlin I struggled a bit with understanding coroutines in a way that I didn't when learning goroutines.

When I learned about Kotlin's [`select`](https://kotlinlang.org/docs/select-expression.html) I realized I could fully
re-apply my goroutine knowledge. I don't have a Kotlin coroutine book on my shelf, but I do have "The Go Programming
Language"

So, for my own self-elucidation **(and hopefully others as well!)** I began rewriting the examples in Kotlin.

I found Kotlin coroutines online quite lacking, so hopefully this helps improve that by proxy.

## Caveats

1. This is **NOT** production code. I don't do great error handling and the code is probably terrible.
2. I'm still new to Kotlin. I did this as a part of learning Kotlin. See point #1
3. Coroutines are available in Kotlin
   via [`kotlinx-coroutines-core`](https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md#using-in-your-projects).
   Coroutines aren't a part of the core language like goroutines are in Go.
4. I use [http4k](https://www.http4k.org/) as a substitute for [Go net/http](https://pkg.go.dev/net/http) for requests
   and server

Of course: if you've got suggestions, please create Pull Requests!

## Coroutine extension pattern

I learned this pattern from: [Kotlin Coroutines in Practice](https://www.youtube.com/watch?v=a3agLJQ6vt8). In-fact, that
talk directly inspired me to work on this.

```kotlin
import kotlin.coroutines.CoroutineContext

// Take a function...
fun fn() {
    /* do fn stuff */
}

// ...and simply change the first line to the following

fun CoroutineContext.fn() = launch {
    /* do fn stuff */
}

// ... and now you can call it within any coroutine scope and it will launch a coroutine


fun main() = runBlocking {
    fn() // now this is semi-equivalent to `go fn()` in Go
}
```

## Links that were useful to me

- [http4k reference](https://www.http4k.org/guide/reference/core/)
- [Default map in Kotlin](https://kotlinexpertise.com/default-map-in-kotlin/)
    - Go's Map operates like a default map where the default is the zero value of the map-value type
- [Simple socket server in Kotlin](https://gist.github.com/Silverbaq/a14fe6b3ec57703e8cc1a63b59605876)
- [Java Socket examples](https://www.codejava.net/java-se/networking/java-socket-client-examples-tcp-ip)
- [Kotlin TCP example](https://sylhare.github.io/2020/04/07/Kotlin-tcp-socket-example.html)
- [Kotlin `use` keyword](https://medium.com/@alekseijegorov/kotlin-use-keyword-31225f80b8c0)
    - `resource.use` auto-closes the resource after the `use` block

## TODO

- Do [cake.go](https://github.com/adonovan/gopl.io/blob/master/ch8/cake/cake.go). It's referenced in the book but not
  included.
- @ 8.5, Write a pseudo image translator: combine cpu and delay for realism.

## License

In no way do the authors of "The Go Programming Language" endorse this or this use.

These examples are licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative
Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.<br/>
<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png"/></a>
