package com.serene.mentor

import android.app.Application
import com.google.firebase.FirebaseApp

class SereneMentorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
