package com.simplemobiletools.commons.activities

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.simplemobiletools.commons.helpers.APP_LICENSES
import com.simplemobiletools.commons.helpers.APP_NAME
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE
import com.simplemobiletools.filepicker.extensions.isShowingWritePermissions
import java.io.File

open class BaseSimpleActivity : AppCompatActivity() {
    lateinit var baseConfig: BaseConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        baseConfig = BaseConfig.newInstance(applicationContext)
        updateBackgroundColor()
        updateActionbarColor()
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateBackgroundColor() {
        window.decorView.setBackgroundColor(baseConfig.backgroundColor)
    }

    fun updateActionbarColor() {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(baseConfig.primaryColor))
        updateStatusbarColor()
    }

    fun updateStatusbarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val hsv = FloatArray(3)
            Color.colorToHSV(baseConfig.primaryColor, hsv)
            hsv[2] *= 0.9f

            window.statusBarColor = Color.HSVToColor(hsv)
        }
    }

    fun updateTextColors(viewGroup: ViewGroup) {
        val cnt = viewGroup.childCount
        (0..cnt - 1).map { viewGroup.getChildAt(it) }
                .forEach {
                    if (it is TextView) {
                        it.setTextColor(baseConfig.textColor)
                    } else if (it is ViewGroup) {
                        updateTextColors(it)
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK && resultData != null) {
            saveTreeUri(resultData)
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun saveTreeUri(resultData: Intent) {
        val treeUri = resultData.data
        baseConfig.treeUri = treeUri.toString()

        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(treeUri, takeFlags)
    }

    fun startAboutActivity(appNameId: Int, licenseMask: Int) {
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            startActivity(this)
        }
    }

    fun launchViewIntent(id: Int) = launchViewIntent(resources.getString(id))

    fun launchViewIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    fun isShowingPermDialog(file: File) = isShowingWritePermissions(file, baseConfig.treeUri, OPEN_DOCUMENT_TREE)
}