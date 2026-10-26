package com.example.lockmyapps

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AppBlockAccessibilityService : AccessibilityService() {
    private val prefs by lazy { getSharedPreferences("lockmyapps", Context.MODE_PRIVATE) }
    private var lastBlockedPackage: String? = null
    private var lastLaunchAt: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event?.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        val selected = prefs.getStringSet("selected", emptySet()) ?: emptySet()
        if (!selected.contains(pkg)) {
            lastBlockedPackage = null
            return
        }
        if (!hasUsageAccess()) return

        val limitMinutes = prefs.getInt("limit_$pkg", 30)
        val usedMs = getTodayUsageForPackage(pkg)
        if (usedMs >= TimeUnit.MINUTES.toMillis(limitMinutes.toLong())) {
            // Debounce repeated accessibility events to avoid opening many lock screens.
            val now = System.currentTimeMillis()
            if (lastBlockedPackage == pkg && now - lastLaunchAt < 1800) return
            lastBlockedPackage = pkg
            lastLaunchAt = now
            val i = Intent(this, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("blocked_app", applicationLabel(pkg))
            }
            startActivity(i)
        }
    }

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        @Suppress("DEPRECATION")
        val mode = ops.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun getTodayUsageForPackage(pkg: String): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()
        val manager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return try {
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                .filter { it.packageName == pkg }
                .sumOf { it.totalTimeInForeground }
        } catch (_: Exception) { 0L }
    }

    private fun applicationLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) { pkg }

    override fun onInterrupt() {}
}
