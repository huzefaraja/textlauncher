package ajar.huzefa.textlauncher

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class AppsAdapter(LIST_TYPE: Int, val listener: AppClickListener, val context: Context, val fixedSize: Boolean = false, private val layoutId: Int = R.layout.list_item_app, var filterString: String = "") : RecyclerView.Adapter<AppsAdapter.AppsViewHolder>(), TextWatcher {
    var listOfFilteredApps: List<App>
    private val setOfApps: Set<App> = if (LIST_TYPE == Constants.ALL_APPS) {
        Launcher.getInstance(context).apps
    } else {
        Launcher.getInstance(context).hiddenApps

    }

    init {
        listOfFilteredApps = ArrayList()
        refresh()
    }

    override fun afterTextChanged(s: Editable?) {
        if (s != null) {
            val searchText = s.toString()
            filterList(searchText)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    fun refresh() {
        Log.d(Companion.TAG, "Refreshing adapter. Filter String: $filterString")
        if (filterString.isBlank()) {
            val list = listOfFilteredApps as ArrayList
            list.clear()
            list.addAll(setOfApps)
        } else {
            listOfFilteredApps = setOfApps.filter { it.name.toLowerCase().contains(filterString.toLowerCase()) }
        }
        notifyDataSetChanged()
    }

    private fun filterList(searchText: String) {
        filterString = searchText
        refresh()
    }


    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    interface AppClickListener {
        fun onAppClick(position: Int, app: App?, view: View?)
        fun onAppHiddenOrShown(app: App?)
        fun onAppUninstall(app: App?)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder =
            AppsViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))


    override fun getItemCount(): Int = listOfFilteredApps.size

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        holder.bind(position)
    }

    private var isNightMode: Boolean = false

    inner class AppsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if (item != null) {
                when (item.itemId) {
                    R.id.action_uninstall -> listener.onAppUninstall(app)
                    R.id.action_info -> app?.openAppInfo(itemView.context)
                    R.id.action_hide -> {
                        if (item.title == itemView.context.getString(R.string.hide)) {
                            app?.hide()
                        } else {
                            app?.show()
                        }
                        listener.onAppHiddenOrShown(app)
                    }
                    R.id.action_increase_size -> {
                        app?.increaseTextSize(); notifyDataSetChanged()
                    }
                    R.id.action_decrease_size -> {
                        app?.decreaseTextSize(); notifyDataSetChanged()
                    }
                }
            }
            return false
        }


        override fun onLongClick(v: View?): Boolean {
            if (v != null) {
                val popupMenu = PopupMenu(v.context, v)
                popupMenu.inflate(R.menu.app_menu)
                popupMenu.setOnMenuItemClickListener(this)
                if (app != null && app!!.isHidden) popupMenu.menu.findItem(R.id.action_hide).title = "Show"
                popupMenu.show()
                return true
            }
            return false
        }

        override fun onClick(v: View?) {
            listener.onAppClick(mPosition, app, v)
        }


        private val tvAppTitle: TextView? = itemView?.findViewById(R.id.tvAppTitle)
        private var app: App? = null
        private var mPosition: Int = -1

        fun bind(position: Int) {
            mPosition = position
            app = listOfFilteredApps[position]
            val app = app
            if (app != null) {
                tvAppTitle?.text = app.name
                if (!fixedSize)
                    tvAppTitle?.textSize = app.textSize.toFloat()
                else tvAppTitle?.textSize = Constants.MIN_TEXT_SIZE.toFloat()
            }
            isNightMode = Launcher.getInstance(context).preferences.getBoolean(context.getString(R.string.pref_night_mode_key), context.resources.getBoolean(R.bool.pref_night_mode_default))
            if (isNightMode) {
                tvAppTitle?.setTextColor(Color.WHITE)
            } else {
                tvAppTitle?.setTextColor(Color.BLACK)
            }
        }

        init {
            itemView?.setOnClickListener(this)
            itemView?.setOnLongClickListener(this)

        }

    }

    companion object {
        @JvmField
        val TAG: String = AppsAdapter::class.java.simpleName
    }


}