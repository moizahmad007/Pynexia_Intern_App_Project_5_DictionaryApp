# Wordly — Dictionary App (Android / Kotlin)

A native Android dictionary app built with Kotlin, XML layouts, Retrofit, Gson, Kotlin coroutines and the free Dictionary API.

## Features
- Search English vocabulary using a REST API
- Word definition and part of speech
- Phonetic spelling and pronunciation audio (when provided by the API)
- Synonyms and example sentence (when available)
- Recent search history stored locally with SharedPreferences
- Asynchronous API calls with loading and error states
- Native XML interface

## Requirements
- Android Studio (recent version)
- JDK 17
- Android SDK 35
- Internet connection

## Run
1. Extract the ZIP.
2. Open the `DictionaryApp` folder in Android Studio.
3. Let Gradle sync and download dependencies.
4. Run on an emulator or Android phone with internet access.

## API
Uses `https://api.dictionaryapi.dev/` (Free Dictionary API). No API key is required. The app currently looks up English words (`/api/v2/entries/en/{word}`).

## Project structure
- `MainActivity.kt` — UI interactions, history, audio playback
- `DictionaryApi.kt` — Retrofit service and JSON data models
- `activity_main.xml` — screen layout
- `AndroidManifest.xml` — internet permission and launcher activity

## Notes
Definitions, synonyms, examples and audio depend on the API response; some words may not provide every field. This is an educational demo, not a full offline dictionary.
