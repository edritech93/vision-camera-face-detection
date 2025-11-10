package com.margelo.nitro.visioncamerafacedetection

import com.facebook.react.bridge.ReactApplicationContext
import java.lang.ref.WeakReference

object ReactContextHolder {
    private var contextRef: WeakReference<ReactApplicationContext>? = null

    fun set(context: ReactApplicationContext) {
        contextRef = WeakReference(context)
    }

    fun get(): ReactApplicationContext? {
        return contextRef?.get()
    }
}
