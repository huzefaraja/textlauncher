package ajar.huzefa.textlauncher

import ajar.huzefa.textlauncher.Constants.MAX_TEXT_SIZE
import ajar.huzefa.textlauncher.Constants.MIN_TEXT_SIZE
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*
import kotlin.math.min

/**
 * Created by huzefa on 3/24/2018.
 */

class Launcher(context: Context) {

    val spTextSizes by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_TEXT_SIZES, Context.MODE_PRIVATE) }
    val spLaunchCounts by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_LAUNCH_COUNTS, Context.MODE_PRIVATE) }
    val spHiddenApps by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_HIDDEN_APPS, Context.MODE_PRIVATE) }
    val preferences by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_LAUNCHER_SETTINGS, Context.MODE_PRIVATE) }
    val apps: TreeSet<App> by lazy { loadApps(context) }
    val hiddenApps = TreeSet<App>()

    private fun loadApps(context: Context): TreeSet<App> {
        Log.d(TAG, "loadApps called")
        val manager = context.packageManager
        val apps = TreeSet<App>()
        hiddenApps.clear()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val availableActivities = manager.queryIntentActivities(i, 0)
        for (availableActivity in availableActivities) {
            val app = App(availableActivity.loadLabel(manager).toString(), availableActivity.activityInfo.packageName, context.applicationContext)
            if (spHiddenApps.contains(availableActivity.activityInfo.packageName)) {
                app.isHidden = true
                hiddenApps.add(app)
            } else {
                apps.add(app)
            }
        }
        Log.d(TAG, "visible apps ${apps.size}")
        Log.d(TAG, "hidden apps ${hiddenApps.size}")
        return apps
    }

    fun hideApp(app: App) {
        spHiddenApps.edit().putBoolean(app.packageName, true).apply()
        apps.remove(app)
        hiddenApps.add(app)
    }

    fun showApp(app: App) {
        spHiddenApps.edit().remove(app.packageName).apply()
        apps.add(app)
        hiddenApps.remove(app)
    }

    private fun refreshApps(context: Context): TreeSet<App> {
        val manager = context.packageManager
        apps.clear()
        hiddenApps.clear()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val availableActivities = manager.queryIntentActivities(i, 0)
        for (availableActivity in availableActivities) {
            val app = App(availableActivity.loadLabel(manager).toString(), availableActivity.activityInfo.packageName, context.applicationContext)
            if (spHiddenApps.contains(availableActivity.activityInfo.packageName)) {
                app.isHidden = true
                hiddenApps.add(app)
            } else {
                apps.add(app)
            }
        }

        return apps
    }

    companion object {
        val TAG = Launcher::class.java.simpleName

        @JvmStatic
        fun getTextSizeFromLaunchCount(launchCount: Int) = min(MAX_TEXT_SIZE, MIN_TEXT_SIZE + (launchCount * 3))

        @Volatile
        private var INSTANCE: Launcher? = null

        @JvmStatic
        fun getInstance(context: Context): Launcher =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Launcher(context).also { INSTANCE = it }
                }
    }
}