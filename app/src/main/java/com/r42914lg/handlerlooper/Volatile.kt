package com.r42914lg.handlerlooper

class VolatileTest {

    inner class UseIt {
        fun doIt() {
            while (!ready) {
                Thread.yield()
            }
            println("DONE")
        }
    }

    companion object {
        // volatile should be here
        var ready = false
    }

    fun runIt() {
        Thread { UseIt().doIt() }.start()
        ready = true
    }
}