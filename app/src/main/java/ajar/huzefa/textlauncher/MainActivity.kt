package ajar.huzefa.textlauncher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), AppsAdapter.AppClickListener, TextWatcher, View.OnClickListener, DrawerLayout.DrawerListener, CompoundButton.OnCheckedChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == Launcher.getInstance(this).preferences) {
            if (getString(R.string.pref_night_mode_key).equals(key)) {
                setColors()
                notifyAdapters()
            } else if (getString(R.string.pref_linear_layout_key).equals(key)) {
                setLayout()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//        if (buttonView == switchNightMode) {
//            if (isChecked) {
//                Launcher.getInstance(this).preferences.edit().putBoolean(getString(R.string.pref_night_mode_key), true).apply()
//            } else {
//                Launcher.getInstance(this).preferences.edit().putBoolean(getString(R.string.pref_night_mode_key), false).apply()
//            }
//            setColors()
//            notifyAdapters()
//        } else if (buttonView == switchLinearLayout) {
//            if (isChecked) {
//                Launcher.getInstance(this).preferences.edit().putBoolean(getString(R.string.pref_linear_layout_key), true).apply()
//            } else {
//                Launcher.getInstance(this).preferences.edit().putBoolean(getString(R.string.pref_linear_layout_key), false).apply()
//            }
//            setLayout()
//        }
    }

    private fun notifyAdapters() {
        appsAdapter?.notifyDataSetChanged()
        hiddenAppsAdapter?.notifyDataSetChanged()
    }

    private fun refreshAdapters() {
        appsAdapter?.refresh()
        hiddenAppsAdapter?.refresh()
    }

    private var isNightMode: Boolean = false

    private fun setColors() {
        isNightMode = Launcher.getInstance(this).preferences.getBoolean(getString(R.string.pref_night_mode_key), resources.getBoolean(R.bool.pref_night_mode_default))
        if (isNightMode) {
            mainActivity.setBackgroundColor(Color.BLACK)
            // leftDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_black)))
            rightDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_black)))
        } else {
            mainActivity.setBackgroundColor(Color.WHITE)
            // leftDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_white)))
            rightDrawer.setBackgroundColor(Color.parseColor(getString(R.string.color_almost_white)))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        Log.d(TAG, "onAppHiddenOrShown: $app")
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
            }
        }
    }

//    inner class FlingDetector : GestureDetector.SimpleOnGestureListener() {
//
//
//        private fun isHorizontal(y1: Float, y2: Float) =
//                (abs(y1 - y2) < HORIZONTAL_SENSITIVITY)
//
//
//        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//
//            if (e2 != null && e1 != null) {
//                if (isHorizontal(e1.y, e2.y))
//                    if (e2.x - e1.x > SENSITIVITY) {
//                        showSearchBar()
//                        openKeyboard()
//                        return true
//                    } else if (e1.x - e2.x > SENSITIVITY) {
//                        clearSearchText()
//                        hideSearchBar()
//                        closeKeyboard()
//                        return true
//                    }
//            }
//
//            return super.onFling(e1, e2, velocityX, velocityY)
//        }
//    }

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
    }

    private fun hideSearchBar() {
        flSearchBar.visibility = GONE
    }

    private fun clearSearchText() {
        etSearch.text.clear()
        tvClearSearchText.text = getString(R.string.close_small)
    }

//    fun adjustRecyclerViewConstraints(attachToTop: Boolean) {
//        var params = rvAppsList.layoutParams as ConstraintLayout.LayoutParams
//        params = ConstraintLayout.LayoutParams(params)
//        if (attachToTop)
//            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
//        else
//            params.topToTop = ConstraintLayout.NO_ID
//        rvAppsList.layoutParams = params
//    }

    private fun openKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        etSearch.requestFocus()
        inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun closeKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
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

//    private lateinit var mDetector: GestureDetectorCompat
//    private lateinit var mFlingDetector: FlingDetector

    private fun loadApps() {
        rvAppsList.adapter = null
        var filterString: String? = ""

        if (appsAdapter != null) {
            etSearch.removeTextChangedListener(appsAdapter)
            filterString = appsAdapter?.filterString
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
        setContentView(R.layout.activity_main)

        setColors()
        setLayout()
        //setupKeyboardToggleListeners()

//        switchNightMode.isChecked = isNightMode
//        switchNightMode.setOnCheckedChangeListener(this)
//        switchLinearLayout.isChecked = isLinearLayout
//        switchLinearLayout.setOnCheckedChangeListener(this)

        etSearch.addTextChangedListener(this)
        // mFlingDetector = FlingDetector()
        // mDetector = GestureDetectorCompat(this, mFlingDetector)
        hideSearchBar()
        mainActivity.addDrawerListener(this)
        loadApps()

        Launcher.getInstance(this).preferences.registerOnSharedPreferenceChangeListener(this)

        leftDrawer.setOnTouchListener { _, _ -> true }

    }

//    private fun setupKeyboardToggleListeners() {
//        mainActivity.viewTreeObserver.addOnGlobalLayoutListener({
//            val r = Rect()
//            mainActivity.getWindowVisibleDisplayFrame(r)
//            val screenHeight = mainActivity.rootView.height
//
//            // r.bottom is the position above soft keypad or device button.
//            // if keypad is shown, the r.bottom is smaller than that before.
//            val keypadHeight = screenHeight - r.bottom
//
//            Log.d(TAG, "keypadHeight = $keypadHeight")
//
//            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
//                // keyboard is opened
////                adjustRecyclerViewConstraints(false)
//                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
//                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            } else {
//                // keyboard is closed
////                adjustRecyclerViewConstraints(true)
//                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
//            }
//        })
//    }

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
                if (mainActivity.isDrawerOpen(leftDrawer))
                    mainActivity.closeDrawer(leftDrawer)
                if (mainActivity.isDrawerOpen(rightDrawer))
                    mainActivity.closeDrawer(rightDrawer)
                if (flSearchBar.visibility == VISIBLE) {
                    hideSearchBar()
                    closeKeyboard()
                } else {
                    showSearchBar()
                    openKeyboard()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (mainActivity.isDrawerOpen(leftDrawer))
            mainActivity.closeDrawer(leftDrawer)
        else if (mainActivity.isDrawerOpen(rightDrawer))
            mainActivity.closeDrawer(rightDrawer)
        else if (flSearchBar.visibility == VISIBLE)
            hideSearchBar()
    }

    class LauncherSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

            if (isAdded)
                if (getString(R.string.pref_night_mode_key).equals(key)) {
                    val isNightMode = sharedPreferences?.getBoolean(key, resources.getBoolean(R.bool.pref_night_mode_default))
                    if (isNightMode != null)
                        if (isNightMode) {
                            // TODO()
                        } else {
                            // TODO()
                        }
                }

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (isAdded) {
                preferenceManager.sharedPreferencesName = Constants.SHARED_PREFERENCES_LAUNCHER_SETTINGS
                addPreferencesFromResource(R.xml.launcher_preferences_fragment)
                preferenceManager.sharedPreferences
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (isAdded)
                preferenceScreen.sharedPreferences
                        .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            if (isAdded) preferenceScreen.sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(this)
        }


    }


}
