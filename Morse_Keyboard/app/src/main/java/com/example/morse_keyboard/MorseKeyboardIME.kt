package com.example.morse_keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

class MorseKeyboardIME : InputMethodService(), TextToSpeech.OnInitListener {

    private var currentMorse = ""
    private var currentWord = ""
    private var lastActionWasSpace = false
    private var tts: TextToSpeech? = null
    private var vibrator: Vibrator? = null
    private var speakLetters = true
    private var speakWords = true
    private var isHelpExpanded = false
    private var isSettingsExpanded = false
    private var speechRate = 1.0f

    private lateinit var morsePreview: TextView
    private lateinit var btnToggleLetters: Button
    private lateinit var btnToggleWords: Button
    private lateinit var helpMenu: LinearLayout
    private lateinit var settingsMenu: LinearLayout
    private lateinit var speedIndicator: TextView

    private val morseToChar = mapOf(
        ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
        "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
        "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
        ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
        "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y",
        "--.." to "Z", ".----" to "1", "..---" to "2", "...--" to "3", "....-" to "4",
        "....." to "5", "-...." to "6", "--..." to "7", "---.." to "8", "----." to "9",
        "-----" to "0"
    )

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

        // Initialize TTS and Vibrator
        tts = TextToSpeech(this, this)
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Get views
        morsePreview = keyboardView.findViewById(R.id.morsePreview)
        btnToggleLetters = keyboardView.findViewById(R.id.btnToggleLetters)
        btnToggleWords = keyboardView.findViewById(R.id.btnToggleWords)
        helpMenu = keyboardView.findViewById(R.id.helpMenu)
        settingsMenu = keyboardView.findViewById(R.id.settingsMenu)
        speedIndicator = keyboardView.findViewById(R.id.speedIndicator)
        val btnDot = keyboardView.findViewById<Button>(R.id.btnDot)
        val btnDash = keyboardView.findViewById<Button>(R.id.btnDash)
        val btnSpace = keyboardView.findViewById<Button>(R.id.btnSpace)
        val btnDelete = keyboardView.findViewById<Button>(R.id.btnDelete)
        val btnHelp = keyboardView.findViewById<Button>(R.id.btnHelp)
        val btnSettings = keyboardView.findViewById<Button>(R.id.btnSettings)

        // Set up help menu buttons
        val helpDot = keyboardView.findViewById<Button>(R.id.helpDot)
        val helpDash = keyboardView.findViewById<Button>(R.id.helpDash)
        val helpSpace = keyboardView.findViewById<Button>(R.id.helpSpace)
        val helpDelete = keyboardView.findViewById<Button>(R.id.helpDelete)

        helpDot.setOnClickListener { demoButton("Dot", "dot") }
        helpDash.setOnClickListener { demoButton("Dash", "dash") }
        helpSpace.setOnClickListener { demoButton("Space", "space") }
        helpDelete.setOnClickListener { demoButton("Delete", "delete") }

        // Set up settings menu buttons
        val btnSpeedSlow = keyboardView.findViewById<Button>(R.id.btnSpeedSlow)
        val btnSpeedNormal = keyboardView.findViewById<Button>(R.id.btnSpeedNormal)
        val btnSpeedFast = keyboardView.findViewById<Button>(R.id.btnSpeedFast)

        btnSpeedSlow.setOnClickListener { setSpeechSpeed(0.75f, "Slow") }
        btnSpeedNormal.setOnClickListener { setSpeechSpeed(1.0f, "Normal") }
        btnSpeedFast.setOnClickListener { setSpeechSpeed(1.5f, "Fast") }

        // Set up main button listeners
        btnDot.setOnClickListener { handleDot() }
        btnDash.setOnClickListener { handleDash() }
        btnSpace.setOnClickListener { handleSpace() }
        btnDelete.setOnClickListener { handleDelete() }
        btnToggleLetters.setOnClickListener { toggleLetters() }
        btnToggleWords.setOnClickListener { toggleWords() }
        btnHelp.setOnClickListener { toggleHelpMenu() }
        btnSettings.setOnClickListener { toggleSettingsMenu() }

        updatePreview()
        updateToggleButtons()
        updateSpeedIndicator("Normal")
        return keyboardView
    }

    // Vibration functions - MUST BE DEFINED FIRST
    private fun vibrateDot() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }

    private fun vibrateDash() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    }

    private fun vibrateSpace() {
        val pattern = longArrayOf(0, 300, 50, 300,40,300)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun vibrateDelete() {
        val pattern = longArrayOf(0,200,50,200)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun vibrateError() {
        val pattern = longArrayOf(0, 100, 50, 300, 50, 100)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun vibrateWarning() {
        val pattern = longArrayOf(0, 200, 100, 200)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    // Error handling functions
    private fun handleInvalidMorse() {
        vibrateError()
        speakImmediate("Invalid morse code")
        currentMorse = ""
    }

    private fun handleMaxLengthWarning() {
        vibrateWarning()
        speakImmediate("Maximum length reached")
    }

    // Main button handlers
    private fun handleDot() {
        if (currentMorse.length >= 5) {
            handleMaxLengthWarning()
            return
        }

        currentMorse += "."
        lastActionWasSpace = false
        vibrateDot()
        updatePreview()
    }

    private fun handleDash() {
        if (currentMorse.length >= 5) {
            handleMaxLengthWarning()
            return
        }

        currentMorse += "-"
        lastActionWasSpace = false
        vibrateDash()
        updatePreview()
    }

    private fun handleSpace() {
        vibrateSpace()

        if (currentMorse.isNotEmpty()) {
            val char = morseToChar[currentMorse]
            if (char != null) {
                currentInputConnection?.commitText(char, 1)
                currentWord += char

                if (speakLetters) {
                    speak(char)
                }
                currentMorse = ""
                lastActionWasSpace = true
            } else {
                handleInvalidMorse()
            }
        } else {
            if (lastActionWasSpace) {
                if (currentWord.isNotEmpty() && speakWords) {
                    speakWord(currentWord)
                    currentWord = ""
                }
                currentInputConnection?.commitText(" ", 1)
            }
            lastActionWasSpace = false
        }
        updatePreview()
    }

    private fun handleDelete() {
        vibrateDelete()

        if (currentMorse.isNotEmpty()) {
            currentMorse = currentMorse.dropLast(1)
        } else {
            currentInputConnection?.deleteSurroundingText(1, 0)
            if (currentWord.isNotEmpty()) {
                currentWord = currentWord.dropLast(1)
            }
        }
        lastActionWasSpace = false
        updatePreview()
    }

    private fun toggleLetters() {
        speakLetters = !speakLetters

        if (speakLetters) {
            vibrateDot()
            speakImmediate("Letters on")
        } else {
            vibrateSpace()
            speakImmediate("Letters off")
        }

        updateToggleButtons()
    }

    private fun toggleWords() {
        speakWords = !speakWords

        if (speakWords) {
            vibrateDot()
            speakImmediate("Words on")
        } else {
            vibrateSpace()
            speakImmediate("Words off")
        }

        updateToggleButtons()
    }

    private fun updateToggleButtons() {
        if (speakLetters) {
            btnToggleLetters.setBackgroundResource(R.drawable.button_toggle_on)
        } else {
            btnToggleLetters.setBackgroundResource(R.drawable.button_toggle_off)
        }

        if (speakWords) {
            btnToggleWords.setBackgroundResource(R.drawable.button_toggle_on)
        } else {
            btnToggleWords.setBackgroundResource(R.drawable.button_toggle_off)
        }
    }

    private fun toggleHelpMenu() {
        isHelpExpanded = !isHelpExpanded

        if (isHelpExpanded && isSettingsExpanded) {
            isSettingsExpanded = false
            settingsMenu.visibility = View.GONE
        }

        if (isHelpExpanded) {
            helpMenu.visibility = View.VISIBLE
            vibrateDot()
            speakImmediate("Help menu opened")
        } else {
            helpMenu.visibility = View.GONE
            vibrateDot()
            speakImmediate("Help menu closed")
        }
    }

    private fun toggleSettingsMenu() {
        isSettingsExpanded = !isSettingsExpanded

        if (isSettingsExpanded && isHelpExpanded) {
            isHelpExpanded = false
            helpMenu.visibility = View.GONE
        }

        if (isSettingsExpanded) {
            settingsMenu.visibility = View.VISIBLE
            vibrateDot()
            speakImmediate("Settings menu opened")
        } else {
            settingsMenu.visibility = View.GONE
            vibrateDot()
            speakImmediate("Settings menu closed")
        }
    }

    private fun demoButton(name: String, vibrationType: String) {
        when (vibrationType) {
            "dot" -> vibrateDot()
            "dash" -> vibrateDash()
            "space" -> vibrateSpace()
            "delete" -> vibrateDelete()
        }
        speakImmediate(name)
    }

    private fun setSpeechSpeed(speed: Float, speedName: String) {
        speechRate = speed
        tts?.setSpeechRate(speechRate)
        updateSpeedIndicator(speedName)
        vibrateDot()
        speakImmediate("Speed set to $speedName")
    }

    private fun updateSpeedIndicator(speedName: String = "Normal") {
        speedIndicator.text = "Current: $speedName"
    }

    private fun updatePreview() {
        if (currentMorse.isNotEmpty()) {
            val preview = morseToChar[currentMorse] ?: "?"
            val warningSymbol = if (currentMorse.length >= 5) " ⚠️" else ""
            morsePreview.text = "$currentMorse → $preview$warningSymbol"
        } else if (currentWord.isNotEmpty()) {
            morsePreview.text = "Word: $currentWord (press Space twice to hear)"
        } else {
            morsePreview.text = "Ready to type..."
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakImmediate(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakWord(word: String) {
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(speechRate)
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}