package com.r42914lg.handlerlooper

class SharedResource {
    fun doSomething(tag: String, lock: Any) {
        synchronized(lock) {
            (1..10).forEach {
                println("$tag $it")
            }
        }
    }
}

class User(private val tag: String, private val res: SharedResource) {
    fun act(lock: Any) {
        res.doSomething(tag, lock)
    }
}

class MultipleLocks {
    fun run() {
        val res = SharedResource()

        val lock1 = "Lock 1"
        val lock2 = "Lock 2"

        val t1 = Thread { User("user-1", res).act(lock1) }
        val t2 = Thread { User("user-2", res).act(lock2) }
        val t3 = Thread { User("user-3", res).act(lock1) }
        val t4 = Thread { User("user-4", res).act(lock2) }

        t1.start()
        t2.start()
        t3.start()
        t4.start()
    }
}