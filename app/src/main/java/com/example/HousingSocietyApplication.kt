package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class HousingSocietyApplication : Application() {

    companion object {
        private const val TAG = "HousingSocietyApp"
        lateinit var instance: HousingSocietyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            Log.i(TAG, "Initializing HousingSocietyApplication...")

            // Setup global uncaught exception handler to prevent silent native/runtime crashes
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                Log.e(TAG, "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
                defaultHandler?.uncaughtException(thread, throwable)
            }

            // Verify and initialize Firebase safely
            initializeFirebaseSafely()

            Log.i(TAG, "HousingSocietyApplication initialized successfully.")
        } catch (e: Throwable) {
            Log.e(TAG, "Error during HousingSocietyApplication onCreate: ${e.message}", e)
        }
    }

    private fun initializeFirebaseSafely() {
        try {
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                Log.i(TAG, "Initializing FirebaseApp default instance...")
                FirebaseApp.initializeApp(this)
                Log.i(TAG, "FirebaseApp initialized.")
            } else {
                Log.i(TAG, "FirebaseApp already initialized with ${apps.size} app(s).")
            }
        } catch (e: Throwable) {
            // Non-fatal fallback for environments where google-services or network configuration differs
            Log.w(TAG, "Firebase initialization warning: ${e.message}. Continuing with local offline-first mode.")
        }
    }
}
