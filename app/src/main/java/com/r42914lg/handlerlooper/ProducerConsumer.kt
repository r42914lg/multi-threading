package com.r42914lg.handlerlooper

class ProducerConsumer<T> {
    private val queue = mutableListOf<T>()
    private val lock = Object()

    fun consume(): T {
        synchronized(lock) {
            while (queue.size == 0) {
                lock.wait()
            }
            lock.notify()
            return queue.removeAt(0)
        }
    }

    fun produce(t: T) {
        synchronized(lock) {
            while (queue.size == BUFFER_SIZE) {
                lock.wait()
            }
            queue.add(t)
            lock.notify()
        }
    }

    companion object {
        const val BUFFER_SIZE = 5
    }
}