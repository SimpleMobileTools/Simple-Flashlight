package com.simplemobiletools.flashlight.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import butterknife.ButterKnife
import butterknife.OnClick
import com.simplemobiletools.commons.activities.AboutActivity
import com.simplemobiletools.commons.extensions.beInvisible
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.helpers.LICENSE_OTTO
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.BusProvider
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.simplemobiletools.flashlight.helpers.Utils
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity() {
    companion object {
        private val CAMERA_PERMISSION = 1
        private val MAX_STROBO_DELAY = 2000
        private val MIN_STROBO_DELAY = 30

        private var mBus: Bus? = null
    }

    private var mCameraImpl: MyCameraImpl? = null
    private var mJustGrantedPermission: Boolean = false

    private val isCameraPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        mBus = BusProvider.instance
        changeIconColor(R.color.translucent_white, bright_display_btn)
        changeIconColor(R.color.translucent_white, stroboscope_btn)
        stroboscope_bar.max = MAX_STROBO_DELAY - MIN_STROBO_DELAY
        stroboscope_bar.progress = stroboscope_bar.max / 2

        stroboscope_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                val frequency = stroboscope_bar.max - progress + MIN_STROBO_DELAY
                if (mCameraImpl != null)
                    mCameraImpl!!.setStroboFrequency(frequency)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                return true
            }
            R.id.about -> {
                startActivity(Intent(applicationContext, AboutActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        startAboutActivity(R.string.app_name, LICENSE_KOTLIN or LICENSE_OTTO, BuildConfig.VERSION_NAME)
    }

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl(this)
        mCameraImpl!!.enableFlashlight()
    }

    @OnClick(R.id.toggle_btn)
    fun toggleFlashlight() {
        mCameraImpl!!.toggleFlashlight()
    }

    @OnClick(R.id.bright_display_btn)
    fun launchBrightDisplay() {
        startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
    }

    @OnClick(R.id.stroboscope_btn)
    fun tryToggleStroboscope() {
        toggleStroboscope()
    }

    private fun toggleStroboscope() {
        // use the old Camera API for stroboscope, the new Camera Manager is way too slow
        if (isCameraPermissionGranted || Utils.isNougat) {
            if (mCameraImpl!!.toggleStroboscope()) {
                stroboscope_bar.visibility = if (stroboscope_bar.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
                changeIconColor(if (stroboscope_bar.visibility == View.VISIBLE) R.color.colorPrimary else R.color.translucent_white, stroboscope_btn)
            }
        } else {
            val permissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION) {
            mJustGrantedPermission = true
            if (isCameraPermissionGranted) {
                toggleStroboscope()
            } else {
                Utils.showToast(applicationContext, R.string.camera_permission)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mBus!!.register(this)

        if (mCameraImpl == null) {
            setupCameraImpl()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mJustGrantedPermission) {
            mJustGrantedPermission = false
            return
        }
        mCameraImpl!!.handleCameraSetup()
        mCameraImpl!!.checkFlashlight()

        bright_display_btn!!.visibility = if (config.brightDisplay) View.VISIBLE else View.GONE
        stroboscope_btn!!.visibility = if (config.stroboscope) View.VISIBLE else View.GONE
        if (!config.stroboscope) {
            mCameraImpl!!.stopStroboscope()
            stroboscope_bar.visibility = View.INVISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        mBus!!.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    private fun releaseCamera() {
        if (mCameraImpl != null) {
            mCameraImpl!!.releaseCamera()
            mCameraImpl = null
        }
    }

    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        if (event.isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    fun enableFlashlight() {
        changeIconColor(R.color.colorPrimary, toggle_btn)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        changeIconColor(R.color.translucent_white, stroboscope_btn)
        stroboscope_bar.beInvisible()
    }

    fun disableFlashlight() {
        changeIconColor(R.color.translucent_white, toggle_btn)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun changeIconColor(colorId: Int, imageView: ImageView?) {
        val appColor = resources.getColor(colorId)
        imageView!!.background.mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_IN)
    }

    @Subscribe
    fun cameraUnavailable(event: Events.CameraUnavailable) {
        Utils.showToast(this, R.string.camera_error)
        disableFlashlight()
    }
}
