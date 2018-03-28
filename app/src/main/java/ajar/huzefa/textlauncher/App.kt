package ajar.huzefa.textlauncher

import ajar.huzefa.textlauncher.Constants.MAX_TEXT_SIZE
import ajar.huzefa.textlauncher.Constants.MIN_TEXT_SIZE
import ajar.huzefa.textlauncher.Launcher.Companion.getTextSizeFromLaunchCount
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import kotlin.math.max
import kotlin.math.min

data class App(val name: String, val packageName: String, private val context: Context, var isHidden: Boolean = false) : Comparable<App> {


    override fun compareTo(other: App): Int {
        val nameComparison = name.toLowerCase().compareTo(other.name.toLowerCase())
        if (nameComparison == 0) {
            return packageName.toLowerCase().compareTo(other.packageName.toLowerCase())
        } else return nameComparison;
    }

    var launchCount: Int
    var textSize: Int

    init {
        val launchCounts = Launcher.getInstance(context).spLaunchCounts
        val textSizes = Launcher.getInstance(context).spTextSizes
        launchCount = launchCounts.getInt(packageName, 0)
        if (textSizes.contains(packageName)) {
            textSize = textSizes.getInt(packageName, getTextSizeFromLaunchCount(launchCount))
        } else {
            textSize = getTextSizeFromLaunchCount(launchCount)
            textSizes.edit().putInt(packageName, textSize).apply()
        }
    }

    fun increaseLaunchCountAndTextSize() {
        launchCount++
        textSize += 3
        textSize = min(MAX_TEXT_SIZE, textSize)
        val launchCounts = Launcher.getInstance(context).spLaunchCounts
        val textSizes = Launcher.getInstance(context).spTextSizes
        launchCounts.edit().putInt(packageName, launchCount).apply()
        textSizes.edit().putInt(packageName, textSize).apply()
    }

    fun decreaseLaunchCountAndTextSize() {
        launchCount--
        textSize -= 3
        textSize = max(MIN_TEXT_SIZE, textSize)
        val launchCounts = Launcher.getInstance(context).spLaunchCounts
        val textSizes = Launcher.getInstance(context).spTextSizes
        launchCounts.edit().putInt(packageName, launchCount).apply()
        textSizes.edit().putInt(packageName, textSize).apply()
    }


    fun increaseTextSize() {
        textSize += 3
        textSize = min(MAX_TEXT_SIZE, textSize)
        val textSizes = Launcher.getInstance(context).spTextSizes
        textSizes.edit().putInt(packageName, textSize).apply()
    }

    fun decreaseTextSize() {
        textSize -= 3
        textSize = max(MIN_TEXT_SIZE, textSize)
        val textSizes = Launcher.getInstance(context).spTextSizes
        textSizes.edit().putInt(packageName, textSize).apply()
    }

    fun hide() {
        isHidden = true
        Launcher.getInstance(context).hideApp(this)
    }

    fun show() {
        isHidden = false
        Launcher.getInstance(context).showApp(this)
    }

    fun openAppInfo(context: Context?) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${packageName}")
            context?.startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            context?.startActivity(intent)

        }

    }

}