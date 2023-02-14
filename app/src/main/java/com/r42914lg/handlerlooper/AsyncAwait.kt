package com.r42914lg.handlerlooper

import kotlinx.coroutines.*

suspend fun f1(): Int = withContext(Dispatchers.Default) {
    delay(3000)
    99
}

suspend fun f2(number: Int) = withContext(Dispatchers.Default) {
    println(number + 1)
}

//fun main() {
//    runBlocking {
//        val f1Def = async {
//            f1()
//        }
//
//        val f2Job = launch {
//            f2(f1Def.await())
//        }
//    }
//}