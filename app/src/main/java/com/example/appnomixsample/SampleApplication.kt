package com.example.appnomixsample

import android.app.Application
import app.appnomix.sdk.external.CouponsSdkFacade

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CouponsSdkFacade.setup(
            CouponsSdkFacade.Config(
                authToken = "insert-your-auth-token",
                clientId = "insert-your-client-id"
            )
        )
    }
}