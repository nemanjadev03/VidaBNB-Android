package com.example.vidabnb

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VidaBNBApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}