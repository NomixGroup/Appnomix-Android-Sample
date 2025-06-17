package com.example.appnomixsample

import android.app.Application
import app.appnomix.sdk.external.CouponsSdkFacade

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CouponsSdkFacade.setup(
            CouponsSdkFacade.Config(
                authToken = getString(R.string.auth_token),
                clientId = getString(R.string.client_id),
                language = "de"
            )
        )
    }
}