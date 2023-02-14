package com.r42914lg.handlerlooper

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

var test = 0

suspend fun f1(lock: Mutex) = withContext(Dispatchers.Default) {
    lock.withLock {
        repeat(1000) {
            test++
        }
    }
}

//fun main() {
//    val mutex = Mutex()
//
//    runBlocking {
//        val j1 = launch {
//            f1(mutex)
//        }
//
//        val j2 = launch {
//            f1(mutex)
//        }
//
//        j1.join()
//        j2.join()
//
//        println(test)
//    }
//}