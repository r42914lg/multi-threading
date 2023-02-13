package com.r42914lg.handlerlooper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

class CustomHandler : Handler(Looper.getMainLooper()) {

    private fun createMessage(str: String) =
        Message.obtain().also {
            it.data = Bundle().apply {
                putString(ARG_MESSAGE, str)
            }
            it.what = MESSAGE_WHAT_1
        }

    fun scheduleMessage(str: String) {
        removeMessages(MESSAGE_WHAT_1)
        sendMessageDelayed(createMessage(str), DELAY_IN_MILLS)
    }

    fun scheduleRunnable(r: Runnable) {
        postDelayed(r, DELAY_IN_MILLS)
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == MESSAGE_WHAT_1) {
            val str = msg.data.getString(ARG_MESSAGE)
            println("Handled message: $str")
            return
        }
    }



    companion object {
        private const val MESSAGE_WHAT_1 = 1
        private const val ARG_MESSAGE = "my_message"
        private const val DELAY_IN_MILLS = 1000L
    }
}