package com.sahil.jarvislauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import android.widget.Toast
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var appRow: LinearLayout
    private var cyan = Color.rgb(63, 216, 255)
    private val whiteBlue = Color.rgb(218, 245, 255)
    private val muted = Color.rgb(104, 161, 184)
    private val palettes = listOf(
        "CYAN" to Color.rgb(63, 216, 255),
        "VIOLET" to Color.rgb(174, 111, 255),
        "EMERALD" to Color.rgb(54, 235, 171),
        "AMBER" to Color.rgb(255, 184, 77),
        "ROSE" to Color.rgb(255, 91, 145),
        "ICE BLUE" to Color.rgb(105, 155, 255)
    )
    private var paletteIndex = 0
    private var rgbEnabled = false
    private val prefs by lazy { getSharedPreferences("sahil_theme", MODE_PRIVATE) }
    private val rgbHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val apps = mutableListOf<android.content.pm.ResolveInfo>()
    private var selected = 0
    private var clockView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        paletteIndex = prefs.getInt("palette", 0).coerceIn(0, palettes.lastIndex)
        rgbEnabled = prefs.getBoolean("rgb_enabled", false)
        cyan = palettes[paletteIndex].second
        buildUi()
        loadApps()
        renderApps()
        updateClock()
        rgbHandler.post(object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    if (rgbEnabled) {
                        cyan = Color.HSVToColor(floatArrayOf((System.currentTimeMillis() / 18L % 360L).toFloat(), 0.82f, 1f))
                        applyAccentToViews(root)
                    }
                    rgbHandler.postDelayed(this, 120)
                }
            }
        })
        appRow.postDelayed(object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    updateClock()
                    appRow.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun bg(stroke: Int = Color.argb(150, 45, 196, 255)): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.argb(210, 3, 17, 31))
            cornerRadius = 26f
            setStroke(2, stroke)
        }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 22, 30, 22)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(1, 4, 12), Color.rgb(4, 18, 38), Color.rgb(1, 6, 16))
            )
        }
        setContentView(root)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = label("S.A.H.I.L.", 28f, cyan, true).apply { tag = "accentText" }
        brand.letterSpacing = .14f
        header.addView(brand, LinearLayout.LayoutParams(0, 58, 1f))
        clockView = label("", 17f, whiteBlue, true).apply { gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL }
        header.addView(clockView, LinearLayout.LayoutParams(260, 58))
        root.addView(header)

        val line = View(this).apply { setBackgroundColor(cyan); tag = "accentLine" }
        root.addView(line, LinearLayout.LayoutParams(-1, 2))

        val subtitle = label("SMART ASSISTANT  //  HOME INTERFACE", 11f, muted, true)
        subtitle.setPadding(2, 12, 2, 14)
        root.addView(subtitle)

        val themeControls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 10)
        }
        val colorButton = Button(this).apply {
            text = "COLOR: ${palettes[prefs.getInt("palette", 0).coerceIn(0, palettes.lastIndex)].first}  ›"
            textSize = 12f
            isAllCaps = false
            setTextColor(whiteBlue)
            background = bg(cyan)
            setOnClickListener {
                paletteIndex = (paletteIndex + 1) % palettes.size
                prefs.edit().putInt("palette", paletteIndex).apply()
                cyan = palettes[paletteIndex].second
                if (rgbEnabled) rgbEnabled = false
                prefs.edit().putBoolean("rgb_enabled", false).apply()
                buildUi()
                renderApps()
            }
        }
        themeControls.addView(colorButton, LinearLayout.LayoutParams(220, 48))
        val rgbButton = Button(this).apply {
            text = if (rgbEnabled) "RGB: ON  ●" else "RGB: OFF  ○"
            textSize = 12f
            isAllCaps = false
            setTextColor(whiteBlue)
            background = bg(if (rgbEnabled) cyan else Color.argb(100, 45, 196, 255))
            setOnClickListener {
                rgbEnabled = !rgbEnabled
                prefs.edit().putBoolean("rgb_enabled", rgbEnabled).apply()
                if (!rgbEnabled) cyan = palettes[paletteIndex].second
                buildUi()
                renderApps()
            }
        }
        val rgbParams = LinearLayout.LayoutParams(190, 48)
        rgbParams.setMargins(12, 0, 0, 0)
        themeControls.addView(rgbButton, rgbParams)
        root.addView(themeControls)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = bg(Color.argb(110, 37, 185, 255))
            setPadding(20, 14, 20, 14)
        }
        val core = label("◉", 94f, cyan, true).apply { gravity = Gravity.CENTER; tag = "accentText" }
        hero.addView(core, LinearLayout.LayoutParams(-1, 110))
        hero.addView(label("WELCOME BACK, SAHIL", 22f, whiteBlue, true).apply { gravity = Gravity.CENTER })
        hero.addView(label("Your personal entertainment system is online", 14f, muted).apply { gravity = Gravity.CENTER })
        hero.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.argb(220, 5, 35, 67), Color.argb(180, 2, 12, 28))).apply { cornerRadius = 28f; setStroke(2, Color.argb(180, 53, 226, 255)) }
        root.addView(hero, LinearLayout.LayoutParams(-1, 0, 1f))

        root.addView(label("YOUR APPLICATIONS", 14f, cyan, true).apply {
            tag = "accentText"
            setPadding(2, 16, 2, 8)
            letterSpacing = .12f
        })

        val scroller = ScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        appRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        scroller.addView(appRow)
        root.addView(scroller, LinearLayout.LayoutParams(-1, 126))

        root.addView(label("S.A.H.I.L.  •  PERSONAL MEDIA INTERFACE  •  ORIGINAL GOOGLE TV SYSTEM PRESERVED", 10f, muted).apply {
            setPadding(2, 12, 2, 0)
        })
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val pm = packageManager
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val tvApps = pm.queryIntentActivities(leanback, PackageManager.MATCH_ALL)
        val normal = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val regularApps = pm.queryIntentActivities(normal, PackageManager.MATCH_ALL)
        val combined = (tvApps + regularApps)
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase(Locale.getDefault()) }
        apps.clear()
        apps.addAll(combined)
    }

    private fun renderApps() {
        appRow.removeAllViews()
        if (apps.isEmpty()) {
            appRow.addView(label("No launchable apps found", 16f, muted))
            return
        }
        apps.forEachIndexed { index, info ->
            val title = info.loadLabel(packageManager).toString()
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = bg(if (index == selected) cyan else Color.argb(100, 45, 196, 255))
            tag = "accentTile"
                setPadding(14, 10, 14, 10)
                isFocusable = true
                isFocusableInTouchMode = true
                contentDescription = title
                setOnClickListener { launch(index) }
                setOnFocusChangeListener { v, hasFocus ->
                    v.background = bg(if (hasFocus) cyan else Color.argb(100, 45, 196, 255))
                }
            }
            val icon = TextView(this).apply {
                val drawable = info.loadIcon(packageManager)
                setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                gravity = Gravity.CENTER
                text = ""
            }
            tile.addView(icon, LinearLayout.LayoutParams(62, 48))
            tile.addView(TextView(this).apply {
                text = title
                textSize = 13f
                setTextColor(whiteBlue)
                gravity = Gravity.CENTER
                maxLines = 2
            }, LinearLayout.LayoutParams(108, 36))
            val params = LinearLayout.LayoutParams(150, 116)
            params.setMargins(0, 0, 10, 0)
            appRow.addView(tile, params)
        }
        appRow.getChildAt(selected.coerceIn(0, apps.size - 1))?.requestFocus()
    }

    private fun launch(index: Int) {
        if (index !in apps.indices) return
        val ri = apps[index]
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
                }
                startActivity(fallback)
            } catch (_: Exception) {
                Toast.makeText(this, "Could not open " + ri.loadLabel(packageManager), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyAccentToViews(view: View) {
        when (view.tag) {
            "accentText" -> (view as? TextView)?.setTextColor(cyan)
            "accentLine" -> view.setBackgroundColor(cyan)
            "accentTile" -> {
                val focused = view.hasFocus()
                view.background = bg(if (focused) cyan else Color.argb(100, 45, 196, 255))
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyAccentToViews(view.getChildAt(i))
        }
    }

    private fun updateClock() {
        val now = Date()
        clockView?.text = SimpleDateFormat("hh:mm:ss a  •  EEE, dd MMM", Locale.getDefault()).format(now)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                selected = (selected - 1).coerceAtLeast(0)
                appRow.getChildAt(selected)?.requestFocus()
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                selected = (selected + 1).coerceAtMost(appRow.childCount - 1)
                appRow.getChildAt(selected)?.requestFocus()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val focused = currentFocus
                val idx = (0 until appRow.childCount).firstOrNull { i ->
                    val tile = appRow.getChildAt(i)
                    tile === focused || (tile as? ViewGroup)?.let { group ->
                        focused != null && focused.parent === group
                    } == true
                }
                if (idx != null) { launch(idx); return true }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
