package ajar.huzefa.textlauncher

import ajar.huzefa.textlauncher.Constants.BROADCAST_APPS_LOADED
import ajar.huzefa.textlauncher.Constants.EXTRA_SCROLL_Y
import ajar.huzefa.textlauncher.Constants.EXTRA_SEARCH_TEXT
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), AppsAdapter.AppClickListener, TextWatcher, View.OnClickListener, DrawerLayout.DrawerListener, SharedPreferences.OnSharedPreferenceChangeListener, TextView.OnEditorActionListener {

    override fun onAppUninstall(app: App?) {
        if (app != null) {
            val packageURI = Uri.parse("package:${app.packageName}")
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI)
            startActivity(uninstallIntent)

        }
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        val listOfFilteredApps = appsAdapter?.listOfFilteredApps
        return if (listOfFilteredApps != null && listOfFilteredApps.isNotEmpty()) {
            launchApp(listOfFilteredApps[0])
            true
        } else false
    }

    private val appsRefreshedReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "appsRefreshedReceiver onReceive")
                if (context != null && intent != null) {
                    Log.d(TAG, "appsRefreshedReceiver action ${intent.action}")
                    if (intent.action == Constants.BROADCAST_APPS_LOADED) {
                        Log.d(TAG, "appsRefreshedReceiver notifyingAdapters")
                        refreshAdapters()
                    }
                } else {
                    Log.d(TAG, "appsRefreshedReceiver context or intent null")
                }
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume Called")
        super.onResume()
        Launcher.getInstance(applicationContext).refreshApps(applicationContext)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == Launcher.getInstance(this).preferences) {
            if (getString(R.string.pref_night_mode_key) == key || getString(R.string.pref_wallpaper_key) == key) {
                Launcher.getInstance(this).restartActivity(this,
                        if (areHiddenAppsLoaded) Constants.ACTION_LOAD_HIDDEN_APPS
                        else null, arrayOf(
                        Pair(Constants.EXTRA_SCROLL_Y, rvAppsList.computeVerticalScrollOffset().toString()),
                        Pair(Constants.EXTRA_SEARCH_TEXT, etSearch.text.toString()))
                )
            } else if (getString(R.string.pref_linear_layout_key) == key) {
                setLayout()
            } else if (getString(R.string.pref_transparency_key) == key) {
                setColors()
            }
        }
    }

    private fun refreshAdapters() {
        appsAdapter?.refresh()
        hiddenAppsAdapter?.refresh()
    }

    private var isNightMode: Boolean = false

    private fun setColors() {
        isNightMode = Launcher.getInstance(this).preferences.getBoolean(getString(R.string.pref_night_mode_key), resources.getBoolean(R.bool.pref_night_mode_default))

        val alpha = (255 - (Launcher.getInstance(this).preferences.getInt(getString(R.string.pref_transparency_key), resources.getInteger(R.integer.pref_transparency_default)) * 2.55)).roundToInt()


        if (isNightMode) {
            mainActivity.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
            fabSearch.setColorFilter(Color.WHITE)
            fabSettings.setColorFilter(Color.WHITE)
            fabCloseSettings.setColorFilter(Color.WHITE)
//            leftDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_black)))
            settingsFragmentContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.translucentBlack))
            rightDrawer.setBackgroundColor(ContextCompat.getColor(this, R.color.translucentBlack))
        } else {
            mainActivity.setBackgroundColor(Color.argb(alpha, 255, 255, 255))
            fabSearch.setColorFilter(Color.BLACK)
            fabSettings.setColorFilter(Color.BLACK)
            fabCloseSettings.setColorFilter(Color.BLACK)
//            leftDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_white)))
            settingsFragmentContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.translucentWhite))
            rightDrawer.setBackgroundColor(ContextCompat.getColor(this, R.color.translucentWhite))
        }
//        Launcher.getInstance(this).setTheme(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appsRefreshedReceiver)
        Launcher.getInstance(this).preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    private var areHiddenAppsLoaded: Boolean = false

    override fun onDrawerOpened(drawerView: View) {
        if (drawerView == rightDrawer && !areHiddenAppsLoaded)
            loadHiddenApps()
    }

    override fun onAppHiddenOrShown(app: App?) {
        refreshAdapters()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v) {
                tvClearSearchText -> {
                    if (etSearch.text.isNotBlank())
                        clearSearchText()
                    else {
                        hideSearchBar()
                        closeKeyboard()
                    }
                }
                fabSearch -> {
                    startSearchMode()
                }
                fabSettings -> {
                    settingsFragmentContainer.visibility = VISIBLE
                }
                fabCloseSettings -> {
                    settingsFragmentContainer.visibility = GONE
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s != null) {
            if (s.isNotEmpty()) {
                tvClearSearchText.text = getString(R.string.clear_small)
            } else {
                tvClearSearchText.text = getString(R.string.close_small)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun showSearchBar() {
        flSearchBar.visibility = VISIBLE
        fabSearch.visibility = GONE
        fabSettings.visibility = GONE
    }

    private fun hideSearchBar() {
        flSearchBar.visibility = GONE
        fabSearch.visibility = VISIBLE
        fabSettings.visibility = VISIBLE
    }

    private fun clearSearchText() {
        etSearch.text.clear()
        tvClearSearchText.text = getString(R.string.close_small)
    }

    private fun openKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        etSearch.requestFocus()
        inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun closeKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun launchApp(app: App) {
        app.increaseLaunchCountAndTextSize()
        startActivity(packageManager.getLaunchIntentForPackage(app.packageName))
        closeKeyboard()
    }

    override fun onAppClick(position: Int, app: App?, view: View?) {
        if (app != null) {
            app.increaseLaunchCountAndTextSize()
            if (view != null)
                startActivity(packageManager.getLaunchIntentForPackage(app.packageName),
                        ActivityOptionsCompat.makeClipRevealAnimation(view,
                                view.x.roundToInt(), view.y.roundToInt(),
                                view.width, view.height).toBundle())
            appsAdapter?.notifyDataSetChanged()
            closeKeyboard()
        }
    }

    private var appsAdapter: AppsAdapter? = null
    private var hiddenAppsAdapter: AppsAdapter? = null

    private fun loadApps() {

        var filterString: String? = ""

        if (appsAdapter != null) {
            etSearch.removeTextChangedListener(appsAdapter)
            filterString = appsAdapter?.filterString
        }
        if (filterString.isNullOrBlank()) {
            filterString = etSearch.text.toString()
        }
        if (isLinearLayout) {
            rvAppsList.layoutManager = LinearLayoutManager(this)
            rvAppsList.setHasFixedSize(true)
            appsAdapter = AppsAdapter(
                    Constants.ALL_APPS,
                    listener = this,
                    context = this.applicationContext,
                    layoutId = R.layout.list_item_app_alternate,
                    fixedSize = true,
                    filterString = filterString.orEmpty()

            )

        } else {
            rvAppsList.setHasFixedSize(false)
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            rvAppsList.layoutManager = layoutManager

            appsAdapter = AppsAdapter(
                    Constants.ALL_APPS,
                    listener = this,
                    context = this.applicationContext,
                    filterString = filterString.orEmpty()
            )

        }
        Log.d(TAG, "Filter String is $filterString")
        rvAppsList.adapter = appsAdapter
        etSearch.addTextChangedListener(appsAdapter)
        Log.d(TAG, "Apps Adapter is ready")

    }

    private fun loadHiddenApps() {
        rvHiddenAppsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvHiddenAppsList.setHasFixedSize(true)
        hiddenAppsAdapter = AppsAdapter(Constants.HIDDEN_APPS,
                listener = this,
                context = this.applicationContext,
                fixedSize = true,
                layoutId = R.layout.list_item_app_alternate

        )
        rvHiddenAppsList.adapter = hiddenAppsAdapter
        areHiddenAppsLoaded = true
    }

    private var isLinearLayout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Launcher.getInstance(this).setTheme(this)
        setContentView(R.layout.activity_main)
        setColors()
        setLayout()

        registerReceiver(appsRefreshedReceiver, IntentFilter(BROADCAST_APPS_LOADED))

        hideSearchBar()

        if (intent != null) {
            if (intent.hasExtra(EXTRA_SEARCH_TEXT)) {
                etSearch.setText(intent.getStringExtra(EXTRA_SEARCH_TEXT))
                intent.removeExtra(EXTRA_SEARCH_TEXT)
            }
        }

        loadApps()

        if (intent != null) {
            if (intent.action != null && intent.action == Constants.ACTION_LOAD_HIDDEN_APPS) {
                loadHiddenApps()
            }
            if (intent.hasExtra(EXTRA_SCROLL_Y)) {
                try {
                    rvAppsList.smoothScrollBy(0, intent.getStringExtra(EXTRA_SCROLL_Y).toInt())
                    intent.removeExtra(EXTRA_SCROLL_Y)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    // rv view.getwidth, view is null
                }
            }
        }

        if (savedInstanceState != null) {
            if (!areHiddenAppsLoaded) {
                if (savedInstanceState.getBoolean(Constants.ACTION_LOAD_HIDDEN_APPS, false)) {
                    loadHiddenApps()
                }
            }
        }



        etSearch.addTextChangedListener(this)
        etSearch.setOnEditorActionListener(this)
        mainActivity.addDrawerListener(this)
        Launcher.getInstance(this).preferences.registerOnSharedPreferenceChangeListener(this)
        settingsFragmentContainer.setOnTouchListener { _, _ -> true }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(Constants.ACTION_LOAD_HIDDEN_APPS, areHiddenAppsLoaded)


    }

    private fun setLayout() {
        isLinearLayout = Launcher.getInstance(this).preferences.getBoolean(getString(R.string.pref_linear_layout_key), resources.getBoolean(R.bool.pref_linear_layout_default))
        if (appsAdapter != null) {
            loadApps()
        }
    }

    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_MAIN == intent.action) {
            val alreadyOnHome = intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            if (alreadyOnHome) {
                handleHomeAndBack(true)
            }
        }
    }

    private fun startSearchMode() {
        showSearchBar()
        openKeyboard()
    }

    private fun stopSearchMode() {
        hideSearchBar()
        closeKeyboard()
    }

    private fun handleHomeAndBack(shouldStartSearchMode: Boolean) {

        when {
            settingsFragmentContainer.visibility == VISIBLE -> settingsFragmentContainer.visibility = GONE
            mainActivity.isDrawerOpen(rightDrawer) -> mainActivity.closeDrawer(rightDrawer)
            flSearchBar.visibility == VISIBLE -> stopSearchMode()
            shouldStartSearchMode -> startSearchMode()
        }

    }

    override fun onBackPressed() {
        handleHomeAndBack(false)
    }

    class LauncherSettingsFragment : PreferenceFragmentCompat() {


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (isAdded) {
                preferenceManager.sharedPreferencesName = Constants.SHARED_PREFERENCES_LAUNCHER_SETTINGS
                addPreferencesFromResource(R.xml.launcher_preferences_fragment)
            }
        }


    }


}
