package com.simplemobiletools.flashlight.activities

import android.os.Bundle
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.flashlight.R
import kotlinx.android.synthetic.main.activity_timer.*


class TimerActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        updateMaterialActivityViews(timer_coordinator, timer_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(timer_nested_scrollview, timer_toolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(timer_toolbar, NavigationIcon.Arrow)
        updateTextColors(timer_holder)
    }
}
