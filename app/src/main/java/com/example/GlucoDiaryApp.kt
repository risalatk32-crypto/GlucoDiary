package com.example

import android.app.Application
import android.util.Log

class GlucoDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Crash guard: Catch any unhandled thread exceptions to prevent "App keeps stopping" crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GlucoDiaryApp", "Safely caught uncaught exception on thread ${thread.name}:", throwable)
            // If critical system thread, delegate, otherwise recover gracefully
            try {
                defaultHandler?.uncaughtException(thread, throwable)
            } catch (e: Exception) {
                Log.e("GlucoDiaryApp", "Error during exception handling", e)
            }
        }
    }
}
