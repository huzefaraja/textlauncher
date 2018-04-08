package ajar.huzefa.textlauncher

import ajar.huzefa.textlauncher.Constants.MAX_TEXT_SIZE
import ajar.huzefa.textlauncher.Constants.MIN_TEXT_SIZE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import java.util.*
import kotlin.math.min

/**
 * Created by huzefa on 3/24/2018.
 */

class Launcher(private val context: Context) {

    val spTextSizes: SharedPreferences by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_TEXT_SIZES, Context.MODE_PRIVATE) }
    val spLaunchCounts: SharedPreferences by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_LAUNCH_COUNTS, Context.MODE_PRIVATE) }
    private val spHiddenApps: SharedPreferences by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_HIDDEN_APPS, Context.MODE_PRIVATE) }
    val preferences: SharedPreferences by lazy { context.getSharedPreferences(Constants.SHARED_PREFERENCES_LAUNCHER_SETTINGS, Context.MODE_PRIVATE) }
    val apps = TreeSet<App>()
    val hiddenApps = TreeSet<App>()

    class AsyncLoadAppsTask : AsyncTask<Context, Unit, Unit>() {

        override fun doInBackground(vararg contexts: Context?) {
            Log.d(TAG, "AsyncLoadAppsTask doInBackground")
            if (contexts.isNotEmpty()) {
                val context = contexts[0]
                if (context != null)
                    Launcher.getInstance(context).loadAppsFromSystem(context)
                else Log.d(TAG, "context is null")
            } else {
                Log.d(TAG, "No context")
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            Log.d(TAG, "AsyncLoadAppsTask onPostExecute")
            Launcher.sendBroadcast(Intent(Constants.BROADCAST_APPS_LOADED))
        }

        init {
            Log.d(TAG, "AsyncLoadAppsTask init")
        }
    }

    private fun loadAppsFromSystem(context: Context) {
        val manager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val availableActivities = manager.queryIntentActivities(intent, 0)
        for (availableActivity in availableActivities) {
            val app = App(availableActivity.loadLabel(manager).toString(), availableActivity.activityInfo.packageName, context.applicationContext)
            if (spHiddenApps.contains(availableActivity.activityInfo.packageName)) {
                app.isHidden = true
                hiddenApps.add(app)
            } else {
                apps.add(app)
            }
        }
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

    fun refreshApps(context: Context) {
        Log.d(TAG, "refreshApps Called")
        apps.clear()
        hiddenApps.clear()
        AsyncLoadAppsTask().execute(context.applicationContext)
    }

    companion object {
        @JvmField
        val TAG: String = Launcher::class.java.simpleName

        @JvmStatic
        fun getTextSizeFromLaunchCount(launchCount: Int) = min(MAX_TEXT_SIZE, MIN_TEXT_SIZE + (launchCount * 3))

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: Launcher? = null

        @JvmStatic
        fun getInstance(context: Context): Launcher =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Launcher(context).also { INSTANCE = it }
                }

        @JvmStatic
        fun sendBroadcast(broadcastIntent: Intent) {
            Log.d(TAG, "Launcher sendBroadcast")
            INSTANCE?.context?.sendBroadcast(broadcastIntent)
        }
    }

    fun setTheme(activity: Activity) {
        val context = activity.applicationContext
        val nightMode = preferences.getBoolean(context.getString(R.string.pref_night_mode_key), context.resources.getBoolean(R.bool.pref_night_mode_default))
        val showWallpaper = preferences.getBoolean(context.getString(R.string.pref_wallpaper_key), context.resources.getBoolean(R.bool.pref_wallpaper_default))
        if (nightMode && showWallpaper)
            activity.setTheme(R.style.AppThemeDark_Wallpaper)
        else if (nightMode && !showWallpaper)
            activity.setTheme(R.style.AppThemeDark_NoWallpaper)
        else if (!nightMode && showWallpaper)
            activity.setTheme(R.style.AppThemeLight_Wallpaper)
        else
            activity.setTheme(R.style.AppThemeLight_NoWallpaper)
    }

    fun restartActivity(activity: Activity, action: String? = null, extras: Array<Pair<String, String>>? = null) {
        val intent = Intent(activity.applicationContext, activity::class.java).setAction(action)
        if (extras != null) for (extra in extras) intent.putExtra(extra.first, extra.second)
        activity.finish()
        activity.applicationContext.startActivity(intent)
    }
}