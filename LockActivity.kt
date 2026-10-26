package com.example.lockmyapps

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class LockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 18, 26)
        window.navigationBarColor = Color.rgb(16, 18, 26)
        val app = intent.getStringExtra("blocked_app") ?: "This app"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(16, 18, 26))
        }
        root.addView(TextView(this).apply {
            text = "🔒"
            textSize = 64f; gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "$app is locked"
            textSize = 28f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(199, 243, 107))
            setPadding(0, 24, 0, 12)
        })
        root.addView(TextView(this).apply {
            text = "You've reached your daily limit.\n\nCome back tomorrow. Your future self will thank you."
            textSize = 17f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        })
        setContentView(root)
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        // No in-app override. Android system navigation may still exit this screen.
        moveTaskToBack(true)
    }
}
