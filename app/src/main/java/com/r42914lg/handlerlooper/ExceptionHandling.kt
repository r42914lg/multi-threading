package com.r42914lg.handlerlooper

import kotlinx.coroutines.*
import java.io.IOException

fun main(): Unit = runBlocking {
//    Test.testDefaultHandler()
//    Test.testCustomHandler()
//    Test.testCancellation()
//    Test.testExceptionAggregation()
//    Test.testExceptionRethrow()
    Test.testWithSupervisorScope()
}

object Test {
    private val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testDefaultHandler() = coroutineScope {
        val job = GlobalScope.launch { // root coroutine with launch
            println("Throwing exception from launch")
            throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
        }

        job.join()
        println("Joined failed job")

        val deferred = GlobalScope.async { // root coroutine with async
            println("Throwing exception from async")
            throw ArithmeticException() // Nothing is printed, relying on user to call await
        }

        try {
            deferred.await()
            println("Unreached")
        } catch (e: ArithmeticException) {
            println("Caught ArithmeticException")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testCustomHandler() = coroutineScope {
        val job = GlobalScope.launch(handler) { // root coroutine with launch
            println("Throwing exception from launch")
            throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
        }

        job.join()
        println("Joined failed job")

        /**
         * For async custom handler DOES NOT work
         */
        val deferred = GlobalScope.async(handler) { // root coroutine with async
            println("Throwing exception from async")
            throw ArithmeticException() // Nothing is printed, relying on user to call await
        }

        try {
            deferred.await()
            println("Unreached")
        } catch (e: ArithmeticException) {
            println("Caught ArithmeticException")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testCancellation() = coroutineScope {
        val job = GlobalScope.launch(handler) {

            launch { // the first child
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }

            launch { // the second child
                delay(10)
                println("Second child throws an exception")
                throw ArithmeticException()
            }
        }

        job.join()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testExceptionAggregation() {
        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE) // it gets cancelled when another sibling fails with IOException
                } finally {
                    throw ArithmeticException() // the second exception
                }
            }
            launch {
                delay(100)
                throw IOException() // the first exception
            }
            delay(Long.MAX_VALUE)
        }
        job.join()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun testExceptionRethrow() {
        val job = GlobalScope.launch(handler) {
            val inner = launch { // all this stack of coroutines will get cancelled
                launch {
                    launch {
                        throw IOException() // the original exception
                    }
                }
            }
            try {
                inner.join()
            } catch (e: CancellationException) {
                println("Rethrowing CancellationException with original cause")
                throw e // cancellation exception is rethrown, yet the original IOException gets to the handler
            }
        }
        job.join()
    }

    suspend fun testWithSupervisorScope() = coroutineScope {
        val supervisor = SupervisorJob()
        with(CoroutineScope(coroutineContext + supervisor)) {

            // launch the first child -- its exception is ignored for this example (don't do this in practice!)
            val firstChild = launch(handler) {
                println("The first child is failing")
                throw AssertionError("The first child is cancelled")
            }

            // launch the second child
            val secondChild = launch {
                firstChild.join()
                // Cancellation of the first child is not propagated to the second child
                println("The first child is cancelled: ${firstChild.isCancelled}, but the second one is still active")
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    // But cancellation of the supervisor is propagated
                    println("The second child is cancelled because the supervisor was cancelled")
                }
            }

            // wait until the first child fails & completes
            firstChild.join()

            println("Cancelling the supervisor")
            supervisor.cancel()

            secondChild.join()
        }
    }
}