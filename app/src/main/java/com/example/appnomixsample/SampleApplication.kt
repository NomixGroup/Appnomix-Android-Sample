package com.example.appnomixsample

import android.app.Application
import app.appnomix.sdk.external.AppnomixCSDK

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppnomixCSDK.initialize(
            clientId = "YOUR_CLIENT_ID",
            authToken = "YOUR_AUTH_TOKEN",
            options = AppnomixCSDK.ConfigurationOptions(
                language = "de"
            )
        )
    }
}