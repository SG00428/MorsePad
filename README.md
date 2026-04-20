# **Morse-Code-Based Conversational Keyboard for Visually Impaired Users**

*A fully custom Android keyboard that enables text entry using Morse code taps — enhanced with audio feedback, haptic patterns, speech output, and an accessible help & settings menu.*

---

## 📌 **Overview**

This project implements a complete **Android Keyboard Service (IME)** designed for users with visual impairments.
Instead of relying on traditional key layouts, the keyboard allows typing using:

* **DOT (·)**
* **DASH (–)**
* **SPACE (letter/word separator)**

The IME provides **text-to-speech**, **distinct vibration patterns**, and a **simple UI** to make Morse input intuitive and fully non-visual.

The included **MainActivity** guides users to enable and select the keyboard from system settings.

---

## 📲 **How to Install & Use**

### **1. Install the App**

You can:

* Download the APK from Releases *(morsepad.apk)*
* OR build it manually through Android Studio

### **2. Enable the Keyboard**

The **MainActivity** helps guide you:

1. Open the app
2. Tap **Enable Keyboard**
   → Opens system IME settings
3. Toggle **Morse Keyboard** ON

### **3. Select the Keyboard**

Tap **Select Keyboard**
→ Opens system IME picker to choose this keyboard.

---

## ✨ **Key Features**

### 🔵 **Morse Input Engine**

* Tap **DOT** or **DASH** to build characters
* Press **SPACE** to:

  * Convert current Morse → Letter
  * Or enter an actual text space
  * Or speak entire word (double-space)

### 🔊 **Speech Output (TTS)**

* Speaks letters as you type (toggle ON/OFF)
* Speaks words after double-space (toggle ON/OFF)
* Adjustable **speech speed**: Slow / Normal / Fast
* Speaks help instructions & menu states

### 📳 **Haptic Feedback**

Every action has a unique vibration pattern:

| Action             | Vibration Pattern |
| ------------------ | ----------------- |
| DOT                | Short vibration   |
| DASH               | Long vibration    |
| SPACE              | Waveform pattern  |
| DELETE             | Two short pulses  |
| INVALID MORSE      | Error vibration   |
| MAX LENGTH WARNING | Warning vibration |

Helps users understand mistakes or actions *without seeing the screen*.

### 🧭 **Help Menu**

An expandable help menu with demo buttons for:

* DOT
* DASH
* SPACE
* DELETE

Each demo speaks the name and vibrates appropriately.

### ⚙️ **Settings Menu**

* Toggle speaking of **letters**
* Toggle speaking of **words**
* Adjust **speech rate**
* Announced + vibrated feedback for every change

### 📝 **Live Preview**

* Shows current Morse sequence + predicted character
* Shows warnings when Morse length reaches limit
* Shows “Word: ____” when building words

### 🗑️ **Smart Delete**

* Delete last Morse symbol
* If Morse empty → delete last typed character
* Keeps track of current word letters while typing

---

## 🛠️ **Building From Source**

1. Clone the repository:

   ```bash
   git clone https://github.com/Rutuj18/Morse-Code-Based-Conversational-Keyboard-for-Visually-Impaired-Users
   ```
2. Open the project in **Android Studio**
3. Let Gradle sync
4. Build & run on device/emulator
5. To export APK:

   ```
   Build → Build APK(s)
   ```

APK will appear here:

```
app/build/outputs/apk/debug/
```

---

## 📁 **Project Structure**

```
.
├── app/
│   ├── src/main/
│   │   ├── java/com/example/morse_keyboard/
│   │   │   ├── MorseKeyboardIME.kt     # Main custom IME logic
│   │   │   └── MainActivity.kt         # Setup screen (enable/select)
│   │   ├── res/layout/
│   │   │   ├── keyboard_view.xml       # Keyboard UI
│   │   │   └── activity_main.xml       # App UI
│   │   └── AndroidManifest.xml
└── README.md
```

---

## 🎛️ **How the Keyboard Works (Under the Hood)**

* Each DOT/DASH updates `currentMorse`
* SPACE triggers:

  * Morse → character conversion
  * Word building
  * Double-space → speak whole word
* Delete updates either:

  * Current Morse
  * Or actual input text
* TTS reacts instantly using `TextToSpeech.QUEUE_FLUSH`
* Haptics use `VibrationEffect` or legacy API
* UI responds through `updatePreview()` and toggle state tracking
* Help/settings menus are collapsible and accessible via TTS

---

## 🧩 **Future Enhancements**

* Predictive text for Morse input
* Custom vibration intensities
* Multi-language support

