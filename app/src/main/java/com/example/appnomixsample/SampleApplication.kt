package com.example.appnomixsample

import android.app.Application
import app.appnomix.sdk.external.AppnomixCSDK

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppnomixCSDK.initialize(
            clientId = "01K1ZF2DF4WTYXDHGCW4KV7RKF",
            authToken = "01K1ZF0V4X8E6V71EYM2XBQTXT",
            options = AppnomixCSDK.ConfigurationOptions(
                language = "de"
            )
        )
    }
}