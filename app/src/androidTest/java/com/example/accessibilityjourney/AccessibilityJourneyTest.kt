package com.example.accessibilityjourney

import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import app.appnomix.sdk.external.CouponsSdkFacade
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class AccessibilityJourneyTest {

    companion object {
        private const val APP_PACKAGE = "com.example.appnomixsample"
        private const val CHROME_PACKAGE = "com.android.chrome"
        private const val USER_INTERACTION_DELAY = 1500L
        private const val TIMEOUT = 3000L
    }

    private lateinit var uiDevice: UiDevice

    @Before
    fun setup() {
        CouponsSdkFacade.configureTestSavings()

        val flags = UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
        Configurator.getInstance().setUiAutomationFlags(flags)
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun accessibilityJourneyTest() {
        uiDevice.executeShellCommand("pm clear $CHROME_PACKAGE")

        launchApp()

        userInteractionDelay()
        val tellMeMoreButton = findObjectByText("Tell me more")
        tellMeMoreButton?.click()

        userInteractionDelay()
        val moreDetailsButton = findObjectByText("See more details")
        moreDetailsButton?.click()

        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.scrollTextIntoView("and copy them.")

        userInteractionDelay()
        val iUnderstandButton = findObjectByText("I understand")
        iUnderstandButton?.click()

        userInteractionDelay()
        val nextButtonStep2 = findObjectByText("Next")
        nextButtonStep2?.click()

        userInteractionDelay()
        val nextButtonStep3 = findObjectByText("Next")
        nextButtonStep3?.click()

        userInteractionDelay()
        val enableButton = findObjectByText("Enable")
        enableButton?.click()

        userInteractionDelay()
        val applicationNameButton = findObjectByText(getAppName())
        applicationNameButton?.click()

        userInteractionDelay()
        val enableAccessibilityAction = findObjectByText("Use ${getAppName()}")
        enableAccessibilityAction?.click()

        userInteractionDelay()
        val allowButton = findObjectByText("Allow")
        allowButton?.click()

        userInteractionDelay()
        uiDevice.pressHome()

        userInteractionDelay()
        launchChrome()

        userInteractionDelay()
        val searchOrTypeUrl = findObjectByText("Search or type URL")
        searchOrTypeUrl?.click()

        userInteractionDelay()
        val urlBar: UiObject = uiDevice.findObject(UiSelector().resourceId("com.android.chrome:id/url_bar"))
        urlBar.click()
        urlBar.setText("appnomix.app")

        userInteractionDelay()
        uiDevice.pressEnter()

        userInteractionDelay()
        val appnomixButton = findObjectByDesc(
            text = "fab_savings",
            customTimeout = 5.seconds.inWholeMilliseconds
        )
        appnomixButton?.click()

        userInteractionDelay(3.seconds.inWholeMilliseconds)
    }

    private fun launchChrome() {
        uiDevice.executeShellCommand("am start -n $CHROME_PACKAGE/com.google.android.apps.chrome.Main")

        val useWithoutAnAccount = findObjectByText("Use without an account")
        useWithoutAnAccount?.let {
            userInteractionDelay()
            useWithoutAnAccount.click()
        }

        val noThanksButton = findObjectByText("No thanks")
        noThanksButton?.let {
            userInteractionDelay()
            noThanksButton.click()
        }

        val gotItButton = findObjectByText("Got it")
        gotItButton?.let {
            userInteractionDelay()
            gotItButton.click()
        }

        val searchOrTypeWebAddressField = findObjectByText("Search or type web address")
        searchOrTypeWebAddressField?.let {
            userInteractionDelay()
            searchOrTypeWebAddressField.click()
        }
    }

    private fun findObjectByText(
        text: String,
        customTimeout: Long? = null
    ): UiObject2? {
        val uiObject = uiDevice.wait(
            { uiDevice.findObject(By.text(text)) },
            customTimeout ?: TIMEOUT
        )
        return uiObject
    }

    private fun findObjectByDesc(
        text: String,
        customTimeout: Long? = null
    ): UiObject2? {
        val uiObject = uiDevice.wait(
            { uiDevice.findObject(By.desc(text)) },
            customTimeout ?: TIMEOUT
        )
        return uiObject
    }

    private fun userInteractionDelay(delay: Long = USER_INTERACTION_DELAY) {
        runBlocking { delay(delay) }
    }

    private fun getAppName(): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(APP_PACKAGE, 0)
        return applicationInfo.loadLabel(packageManager).toString()
    }

    private fun launchApp(withLaunchOnboarding: Boolean = true) {
        uiDevice.executeShellCommand("am start -n $APP_PACKAGE/.MainActivity ${if (withLaunchOnboarding) "-a $APP_PACKAGE.ACTION_ONBOARDING" else ""}")

        val isAppRunning = uiDevice.wait(
            { uiDevice.currentPackageName == APP_PACKAGE },
            TIMEOUT
        )

        assert(isAppRunning)
    }
}