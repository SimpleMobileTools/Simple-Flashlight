package com.simplemobiletools.flashlight.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.activities.AboutActivity
import com.simplemobiletools.commons.activities.CustomizationActivity
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.openDeviceSettings
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.flashlight.R

internal fun Activity.startAboutActivity(
    appNameId: Int, licenseMask: Long, versionName: String, faqItems: ArrayList<FAQItem>, showFAQBeforeMail: Boolean,
    getAppIconIDs: ArrayList<Int> = getAppIconIDs(),
    getAppLauncherName: String = launcherName()
) {
    hideKeyboard()
    Intent(applicationContext, AboutActivity::class.java).apply {
        putExtra(APP_ICON_IDS, getAppIconIDs)
        putExtra(APP_LAUNCHER_NAME, getAppLauncherName)
        putExtra(APP_NAME, getString(appNameId))
        putExtra(APP_LICENSES, licenseMask)
        putExtra(APP_VERSION_NAME, versionName)
        putExtra(APP_FAQ, faqItems)
        putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
        startActivity(this)
    }
}

internal fun Activity.startCustomizationActivity(
    getAppIconIDs: ArrayList<Int> = getAppIconIDs(),
    getAppLauncherName: String = launcherName()
) {
    if (!packageName.contains("slootelibomelpmis".reversed(), true)) {
        if (baseConfig.appRunCount > 100) {
            val label = "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"
            ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {
                launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
            }
            return
        }
    }

    Intent(applicationContext, CustomizationActivity::class.java).apply {
        putExtra(APP_ICON_IDS, getAppIconIDs)
        putExtra(APP_LAUNCHER_NAME, getAppLauncherName)
        startActivity(this)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun Activity.launchChangeAppLanguageIntent() {
    try {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    } catch (e: Exception) {
        openDeviceSettings()
    }
}

private fun getAppIconIDs() = arrayListOf(
    R.mipmap.ic_launcher_red,
    R.mipmap.ic_launcher_pink,
    R.mipmap.ic_launcher_purple,
    R.mipmap.ic_launcher_deep_purple,
    R.mipmap.ic_launcher_indigo,
    R.mipmap.ic_launcher_blue,
    R.mipmap.ic_launcher_light_blue,
    R.mipmap.ic_launcher_cyan,
    R.mipmap.ic_launcher_teal,
    R.mipmap.ic_launcher_green,
    R.mipmap.ic_launcher_light_green,
    R.mipmap.ic_launcher_lime,
    R.mipmap.ic_launcher_yellow,
    R.mipmap.ic_launcher_amber,
    R.mipmap.ic_launcher,
    R.mipmap.ic_launcher_deep_orange,
    R.mipmap.ic_launcher_brown,
    R.mipmap.ic_launcher_blue_grey,
    R.mipmap.ic_launcher_grey_black
)

private fun Context.launcherName() = getString(R.string.app_launcher_name)