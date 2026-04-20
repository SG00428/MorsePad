package com.example.morse_keyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEnable = findViewById<Button>(R.id.btnEnableKeyboard)
        val btnSelect = findViewById<Button>(R.id.btnSelectKeyboard)
        val statusText = findViewById<TextView>(R.id.statusText)

        btnEnable.setOnClickListener {
            // Open keyboard settings to enable
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        btnSelect.setOnClickListener {
            // Show input method picker to select keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // Check if keyboard is enabled
        updateStatus(statusText)
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.statusText)
        updateStatus(statusText)
    }

    private fun updateStatus(statusText: TextView) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledIMEs = imm.enabledInputMethodList

        val isEnabled = enabledIMEs.any {
            it.packageName == packageName
        }

        statusText.text = if (isEnabled) {
            "✅ Morse Keyboard is enabled!\n\nNow select it as your default keyboard."
        } else {
            "❌ Please enable Morse Keyboard in settings."
        }
    }
}