package com.r42914lg.handlerlooper

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    println("\nREGULAR SCOPE")
    Test1.testWithRegularScope()

    println("\nSUPERVISOR SCOPE")
    Test1.testWithSupervisorScope()
}

object Test1 {
    private val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testWithRegularScope() {
        val job = GlobalScope.launch(handler) {

            val job1 = launch(handler) {
                println("Job-1 started")
                delay(1000)
                println("Job-1 failing with exception...")
                println(" ${7 / 0}") // this throws exception & cancels current job
            }

            val job2 = launch {
                try {
                println("Job-2 started")
                delay(2000)
                println("Job-1 is cancelled: ${job1.isCancelled}, in Job-2 now...") // never printed
                delay(Long.MAX_VALUE)
                } finally {
                    println("Job-2 is cancelled")
                }
            }

            job1.join()
            job2.join()
        }
        job.join()
    }

    suspend fun testWithSupervisorScope() = coroutineScope {
        val supervisor = SupervisorJob()
        with(CoroutineScope(coroutineContext + supervisor)) {

            val job1 = launch(handler) {
                println("Job-1 started")
                delay(1000)
                println("Job-1 failing with exception...")
                println(" ${7 / 0}") // this throws exception & cancels current job
            }

            val job2 = launch {
                try {
                    println("Job-2 started")
                    delay(2000)
                    println("Job-1 is cancelled: ${job1.isCancelled}, in Job-2 now...")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Job-2 is cancelled")
                }
            }

            // wait until the first child fails & completes
            job1.join()

            delay(2000)

            println("Cancelling the supervisor")
            supervisor.cancel()

            job2.join()
        }
    }
}