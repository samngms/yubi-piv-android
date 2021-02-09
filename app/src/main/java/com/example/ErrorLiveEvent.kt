package com.example

import android.util.Log

class ErrorLiveEvent(private val tag: String) : SingleLiveEvent<Throwable>() {
    override fun setValue(t: Throwable?) {
        super.setValue(t)
        t?.run {
            Log.e(tag, t.message, t)
        }
    }
}