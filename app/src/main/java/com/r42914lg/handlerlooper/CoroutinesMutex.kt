package com.r42914lg.handlerlooper

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

var test = 0

suspend fun f1(lock: Mutex) = withContext(Dispatchers.Default) {
    lock.withLock {
        repeat(1000) {
            test++
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun f2(cc: CoroutineContext) = withContext(cc) {
    repeat(1000) {
        test++
    }
}

fun main() {
    val mutex = Mutex()

    runBlocking {

        // with mutex
        val j1 = launch {
            f1(mutex)
        }

        val j2 = launch {
            f1(mutex)
        }

        j1.join()
        j2.join()

        println(test)

        // via thread confinement

        val cc = newSingleThreadContext("Single_threaded")

        val j3 = launch {
            f2(cc)
        }

        val j4 = launch {
            f2(cc)
        }

        j3.join()
        j4.join()

        println(test)
    }
}