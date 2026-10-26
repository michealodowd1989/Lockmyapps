package com.example.lockmyapps

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("lockmyapps", Context.MODE_PRIVATE) }
    private val chosen = linkedSetOf<String>()
    private val rows = mutableMapOf<String, CheckBox>()
    private val limits = mutableMapOf<String, EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chosen.addAll(prefs.getStringSet("selected", emptySet()) ?: emptySet())
        buildUi()
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(Color.rgb(16, 18, 26))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply {
            text = "lockmyapps"
            textSize = 30f; setTextColor(Color.rgb(199, 243, 107))
        }
        content.addView(title)
        content.addView(TextView(this).apply {
            text = "Choose distracting apps, set a daily limit, and block them until midnight."
            textSize = 15f; setTextColor(Color.LTGRAY); setPadding(0, dp(8), 0, dp(16))
        })
        content.addView(actionButton("Grant usage access") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        content.addView(actionButton("Enable FocusLock blocking service") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        content.addView(TextView(this).apply {
            text = "Select apps and set daily minutes"
            textSize = 19f; setTextColor(Color.WHITE); setPadding(0, dp(18), 0, dp(10))
        })

        val apps = getLaunchableApps()
        for (app in apps) {
            val pkg = app.packageName
            val appName = packageManager.getApplicationLabel(app).toString()
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                setBackgroundColor(Color.rgb(32, 37, 47))
            }
            val cb = CheckBox(this).apply {
                text = "$appName"
                textSize = 16f; setTextColor(Color.WHITE)
                isChecked = chosen.contains(pkg)
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.rgb(199,243,107))
                setOnCheckedChangeListener { _, checked ->
                    if (checked) chosen.add(pkg) else chosen.remove(pkg)
                }
            }
            rows[pkg] = cb
            row.addView(cb)
            val limit = EditText(this).apply {
                hint = "Daily limit in minutes (e.g. 30)"
                setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setSingleLine(true)
                setText(prefs.getInt("limit_$pkg", 30).toString())
                limits[pkg] = this
            }
            row.addView(limit)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, dp(8))
            content.addView(row, lp)
        }

        content.addView(actionButton("Save limits") {
            val editor = prefs.edit().putStringSet("selected", chosen.toSet())
            for ((pkg, input) in limits) {
                val minutes = input.text.toString().toIntOrNull()?.coerceIn(1, 1440) ?: 30
                editor.putInt("limit_$pkg", minutes)
            }
            editor.apply()
            Toast.makeText(this, "Limits saved", Toast.LENGTH_SHORT).show()
        })
        content.addView(TextView(this).apply {
            text = "\nImportant: this is an early prototype. Android's permission screens must be completed, and enforcement can vary by device. It cannot guarantee an unbreakable lock against force-stop, uninstall, or revoked permissions."
            textSize = 13f; setTextColor(Color.LTGRAY)
        })
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun actionButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            setOnClickListener { action() }
        }

    private fun getLaunchableApps(): List<ApplicationInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos = if (android.os.Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, android.content.pm.PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") pm.queryIntentActivities(intent, 0)
        }
        val own = packageName
        return infos.mapNotNull { it.activityInfo?.applicationInfo }
            .filter { it.packageName != own }
            .distinctBy { it.packageName }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
    }
}
