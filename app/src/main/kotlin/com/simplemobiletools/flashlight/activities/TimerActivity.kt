package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*
import kotlin.system.exitProcess

class TimerActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        updateMaterialActivityViews(timer_coordinator, timer_holder, true)
        setupMaterialScrollListener(timer_nested_scrollview, timer_toolbar)

    }

    override fun onResume() {
        super.onResume()
        setupToolbar(timer_toolbar, NavigationIcon.Arrow)

        updateTextColors(timer_holder)

    }
}

