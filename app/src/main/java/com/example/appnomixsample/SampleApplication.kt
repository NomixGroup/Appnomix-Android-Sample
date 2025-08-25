package com.example.appnomixsample

import android.app.Application
import app.appnomix.sdk.external.AppnomixSdkFacade

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppnomixSdkFacade.setup(
            AppnomixSdkFacade.Config(
                authToken = "01K1ZF0V4X8E6V71EYM2XBQTXT",
                clientId = "01K1ZF2DF4WTYXDHGCW4KV7RKF",
                language = "de"
            )
        )
    }
}