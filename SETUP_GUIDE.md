# SereneMentor — Complete Setup Guide

## 📦 Project Summary

**95 files** | Kotlin + MVVM + Firebase + Material Design 3

---

## 🚀 Step 1: Create Android Studio Project

1. Open Android Studio → **New Project → Empty Activity**
2. Set:
   - Name: `SereneMentor`
   - Package: `com.serene.mentor`
   - Language: Kotlin
   - Min SDK: API 24
3. **Replace all generated files** with the files from this ZIP.

---

## 🔥 Step 2: Firebase Setup

### 2a. Create Firebase Project
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add Project** → name it `SereneMentor`
3. Register your Android app with package name `com.serene.mentor`
4. Download `google-services.json`
5. Place `google-services.json` in `app/` directory

### 2b. Enable Authentication
1. Firebase Console → **Authentication → Sign-in method**
2. Enable **Email/Password**

### 2c. Create Firestore Database
1. Firebase Console → **Firestore Database → Create database**
2. Start in **production mode**
3. Choose a region close to your users

### 2d. Deploy Security Rules
```bash
# Install Firebase CLI if needed
npm install -g firebase-tools

firebase login
firebase init firestore
# Use existing files: firestore.rules and firestore.indexes.json
firebase deploy --only firestore
```

Or paste `firestore.rules` content directly in Firebase Console → Firestore → Rules.

---

## 🔤 Step 3: Add Poppins Fonts

1. Download from [fonts.google.com/specimen/Poppins](https://fonts.google.com/specimen/Poppins)
2. Download these 4 weights:
   - Regular (400) → rename to `poppins_regular.ttf`
   - SemiBold (600) → rename to `poppins_semibold.ttf`
   - Bold (700) → rename to `poppins_bold.ttf`
   - Regular Italic → rename to `poppins_italic.ttf`
3. Place all `.ttf` files in `app/src/main/res/font/`

---

## 📱 Step 4: Build & Run

```bash
# Sync Gradle
./gradlew clean build

# Or just press ▶ in Android Studio
```

**Required permissions** (already in AndroidManifest):
- `RECORD_AUDIO` — requested at runtime on first recording
- `INTERNET` — for Firebase

---

## 📁 Complete File Structure

```
SereneMentor/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/serene/mentor/
│       │   ├── SereneMentorApp.kt
│       │   ├── activities/
│       │   │   ├── SplashActivity.kt
│       │   │   ├── LoginActivity.kt
│       │   │   ├── SignupActivity.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── TopicSelectionActivity.kt
│       │   │   ├── PreparationActivity.kt
│       │   │   ├── RecordingActivity.kt
│       │   │   └── AnalysisActivity.kt
│       │   ├── fragments/
│       │   │   ├── DashboardFragment.kt
│       │   │   ├── HistoryFragment.kt
│       │   │   └── ProfileFragment.kt
│       │   ├── adapters/
│       │   │   ├── GrammarFeedbackAdapter.kt
│       │   │   ├── NextStepsAdapter.kt
│       │   │   └── SessionHistoryAdapter.kt
│       │   ├── models/
│       │   │   └── Models.kt          ← User, Session, AnalysisResult, Topic, Enums
│       │   ├── viewmodels/
│       │   │   ├── MainViewModel.kt
│       │   │   ├── RecordingViewModel.kt
│       │   │   └── AnalysisViewModel.kt
│       │   ├── firebase/
│       │   │   └── FirebaseManager.kt
│       │   └── utils/
│       │       ├── SpeechAnalyzer.kt  ← Core analysis engine
│       │       ├── TopicRepository.kt ← 10+ GD topics
│       │       ├── PreferencesManager.kt
│       │       ├── NetworkMonitor.kt
│       │       ├── OfflineSyncManager.kt
│       │       ├── WaveformView.kt
│       │       └── Extensions.kt
│       └── res/
│           ├── layout/       ← 11 layouts + 3 item layouts
│           ├── drawable/     ← 22 vector drawables + gradients
│           ├── values/       ← colors, strings, themes, dimens, attrs
│           ├── anim/         ← 6 transition animations
│           ├── color/        ← 3 state selectors
│           ├── font/         ← poppins.xml family definition
│           ├── menu/         ← bottom_nav_menu.xml
│           └── raw/          ← pulse.json Lottie animation
├── firestore.rules
├── firestore.indexes.json
├── build.gradle
└── settings.gradle
```

---

## 🧠 Analysis Engine Overview

`SpeechAnalyzer.kt` performs all analysis **locally and offline**:

| Feature | Method | Details |
|---------|--------|---------|
| Filler Word Detection | `detectFillerWords()` | 20+ fillers, word-boundary matched |
| Fluency Score | `calculateFluencyScore()` | WPM (ideal 120-160) + filler penalty |
| Confidence Score | `calculateConfidenceScore()` | Hesitation patterns + completeness + assertive language |
| Grammar Analysis | `detectGrammarIssues()` | Tense errors, repeated words, fragments |
| Strengths | `generateStrengths()` | Positive reinforcement from scores |
| Weaknesses | `generateWeaknesses()` | Most impactful issues only |
| Next Steps | `generateNextSteps()` | Difficulty-aware, actionable tips |

---

## 🗄️ Firestore Schema

### `users/{userId}`
```json
{
  "userId": "string",
  "name": "string",
  "email": "string",
  "createdAt": "timestamp",
  "totalSessions": 0,
  "averageFluency": 0.0,
  "averageConfidence": 0.0
}
```

### `sessions/{sessionId}`
```json
{
  "userId": "string",
  "topic": "string",
  "topicCategory": "TECHNOLOGY | ECONOMY | SOCIAL_ISSUES | ABSTRACT",
  "difficulty": "Beginner | Intermediate | Advanced",
  "transcript": "string",
  "fluencyScore": 0,
  "confidenceScore": 0,
  "grammarScore": 0,
  "durationSeconds": 0,
  "wordCount": 0,
  "wordsPerMinute": 0,
  "fillerWords": { "uh": 3, "um": 1 },
  "grammarFeedback": [ { "original": "...", "suggestion": "...", "explanation": "...", "type": "TENSE" } ],
  "strengths": ["..."],
  "weaknesses": ["..."],
  "nextSteps": ["..."],
  "timestamp": "timestamp"
}
```

---

## 🎨 Design Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `primary` | `#5C6BC0` | Indigo — buttons, progress, highlights |
| `secondary` | `#26A69A` | Teal — confidence score |
| `accent` | `#FF7043` | Deep Orange — grammar score |
| `score_high` | `#22C55E` | ≥75% scores |
| `score_mid` | `#F59E0B` | 50–74% scores |
| `score_low` | `#EF4444` | <50% scores |
| `background` | `#F5F7FA` | App background |
| `surface` | `#FFFFFF` | Card backgrounds |

---

## ⚡ Bonus Features Included

- ✅ **Offline fallback** — sessions saved locally via SharedPreferences + Gson
- ✅ **Auto-sync** — `OfflineSyncManager` syncs when connectivity restores
- ✅ **NetworkMonitor** — LiveData-based connectivity observer
- ✅ **Dark mode** — theme defined in `themes.xml`
- ✅ **MVVM** — MainViewModel, RecordingViewModel, AnalysisViewModel
- ✅ **10 GD topics** — Technology, Economy, Social Issues, Abstract
- ✅ **5 GD frameworks** — PREP, STAR, 3-Point, Pros/Cons, Cause-Effect
- ✅ **Lottie pulse animation** — recording indicator
- ✅ **Custom WaveformView** — animated mic level bars
- ✅ **Security rules** — Firestore rules + indexes
- ✅ **ProGuard rules** — production-ready

---

## 🐛 Troubleshooting

| Issue | Fix |
|-------|-----|
| `google-services.json` missing | Download from Firebase Console |
| Build fails on fonts | Add `.ttf` files to `res/font/` |
| Speech not recognized | Check mic permission on device |
| Firestore permission denied | Deploy `firestore.rules` |
| Lottie animation missing | The `pulse.json` is included in `res/raw/` |
| `ClassNotFoundException` on model | Check ProGuard keeps `com.serene.mentor.models.**` |
