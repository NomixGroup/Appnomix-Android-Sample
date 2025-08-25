package com.example.appnomixsample

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.appnomix.sdk.external.AppnomixEvent
import app.appnomix.sdk.external.AppnomixEventListener
import app.appnomix.sdk.external.AppnomixSdkFacade
import com.example.appnomixsample.ui.theme.AppnomixSampleTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AppnomixSdkState(val isActivated: Boolean)

class MainActivity : ComponentActivity() {
    private val viewModelState = MutableStateFlow(
        AppnomixSdkState(isActivated = false)
    )
    private val uiState = viewModelState
        .stateIn(
            lifecycleScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppnomixSampleTheme {
                AppnomixControls(
                    modifier = Modifier
                        .safeContentPadding()
                        .fillMaxSize(),
                    stateFlow = uiState
                )
            }
        }

        // when telling the user about this Appnomix Offer, which should prompt him to activate the extension,
        // the 'trackOfferDisplay' should be called with a param giving more context of when this was presented to the user
        AppnomixSdkFacade.trackOfferDisplay("Marble Crush - Next Level")

        // Some apps may require a granular control of how or when the user finished the Appnomix Onboarding.
        // this event listeners gives app more insights of what the user did, so the host apps can track their own events, if needed
        AppnomixSdkFacade.registerEventListener(object : AppnomixEventListener {
            override fun onAppnomixEvent(event: AppnomixEvent) {
                when (event) {
                    AppnomixEvent.ONBOARDING_STARTED -> {
                        Log.i("AppnomixSample", "User started Appnomix Onboarding")
                    }

                    AppnomixEvent.ONBOARDING_ABANDONED -> {
                        Log.i(
                            "AppnomixSample",
                            "User left Appnomix Onboarding without activating the extension"
                        )
                    }

                    AppnomixEvent.ONBOARDING_FINISHED -> {
                        Log.i(
                            "AppnomixSample",
                            "User left Appnomix Onboarding and the extension is Activated"
                        )
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModelState.update { state ->
            state.copy(isActivated = AppnomixSdkFacade.isAccessibilityServiceEnabled())
        }
    }
}

@Composable
fun AppnomixControls(
    modifier: Modifier = Modifier,
    stateFlow: StateFlow<AppnomixSdkState>
) {
    val context = LocalContext.current
    val sdkState by stateFlow.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!sdkState.isActivated) {
            Text(
                text = "Appnomix Extension is NOT Activated",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            val onboardingCustomizationJson = stringResource(R.string.onboarding_customization)
            Button(
                onClick = {
                    AppnomixSdkFacade.launchSdkOnboardingActivity(
                        context.findActivity() as Activity,
                        onboardingCustomizationJson
                    )
                }) {
                Text(text = "Launch Onboarding")
            }
        } else {
            Text(
                text = "Congrats!\nAppnomix Extension is Activated",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppnomixSampleTheme {
        AppnomixControls(stateFlow = MutableStateFlow(AppnomixSdkState(isActivated = false)))
    }
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}