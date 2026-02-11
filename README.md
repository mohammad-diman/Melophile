# 🎵 Melophile - Modern Android Music Player

[![Kotlin](https://img.shields.io/badge/Kotlin-Native-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Melophile** is a premium, high-performance local music player for Android. Built with a focus on aesthetics and user experience, it features a stunning **Glassmorphism UI**, dynamic theming, and an innovative wavy progress bar.

---

## ✨ Key Features (The "Premium" Factors)

*   🎨 **Dynamic Theming (Palette API):** The app interface automatically extracts and applies colors from the current song's album art, creating a living, breathing UI.
*   🌊 **Wavy Seek Bar:** A dynamic, squiggly progress bar that reacts to the music's play/pause state—modernizing the standard seek experience.
*   📊 **Smart Dashboard:** 
    *   **Daily Mix:** Intelligent tracking of your most-played songs.
    *   **Listening Stats:** Real-time data on total plays and your top artist.
*   📱 **Background Playback (Media3 Session):** Full support for background audio with interactive notifications and lock-screen controls using the latest Google Media3 standards.
*   🌙 **Glassmorphism Design:** A dark-themed, translucent interface inspired by modern design trends, utilizing **San Francisco Pro Display** typography.

## 🛠️ Functional Features

*   📂 **Directory Selection:** Choose specific folders to scan for music using Storage Access Framework (SAF).
*   🔍 **Unified Search:** Fast, real-time filtering of songs and artists directly on the Dashboard and Library.
*   ⏳ **Sleep Timer:** Countdown timer to automatically pause music, perfect for late-night listening.
*   📝 **Metadata Editor:** Edit song titles and artist names on-the-fly.
*   🔄 **Playback Modes:** Advanced Shuffle and Repeat (One/All) logic.

---

## 🚀 Tech Stack & Architecture

This project is built using modern Android development (MAD) practices:

*   **Language:** 100% Kotlin
*   **UI:** Jetpack Compose (Declarative UI)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Audio Engine:** Media3 ExoPlayer & MediaSession
*   **Image Loading:** Coil
*   **Color Extraction:** Palette API
*   **State Management:** Compose State & ViewModel

---

## 📸 Screenshots

-

---

## 📥 Installation

1.  Clone this repository:
    ```bash
    git clone https://github.com/yourusername/Melophile.git
    ```
2.  Open the project in **Android Studio (Hedgehog or newer)**.
3.  Sync project with Gradle files.
4.  (Optional) Add your custom SF Pro Display fonts in `res/font/`.
5.  Build and Run on your device.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Melophile** - *Feel the Music, See the Vibes.* 🚀
