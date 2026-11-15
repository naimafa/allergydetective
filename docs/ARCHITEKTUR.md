# Architektur: Allergie-Detektiv

**Stand:** 14.11.2026

## Zielbild & aktuelle Phase
* **Ziel:** Eine Codebasis, die am Ende auf **Android und iOS** läuft.
* **Jetzt (MVP):** Entwicklung unter **Windows**, Tests nur für **Android** (Emulator + eigenes Gerät).
* **Später:** iOS-Build, sobald der Self-Test auf Android erfolgreich ist.

> **Regel für jede Code-Entscheidung:** Nichts einbauen, was nur auf Android funktioniert, außer hinter einer Schnittstelle in `androidMain`. Best Practices und Design Patterns anwenden, sobald sinnvoll (siehe [CODING_RICHTLINIEN.md](CODING_RICHTLINIEN.md)).

---

## Technologie
* **Kotlin Multiplatform (KMP)** + **Compose Multiplatform**
* **Room (KMP)** mit SQLite für lokale Daten
* Coroutines & Flow, ViewModel (KMP)
* Entwicklung in **Android Studio** unter Windows
* **Projektordner:** `D:\workspace\allergydetective`
* **Package / App-ID:** `com.nfar.allergydetective` (vorläufig, siehe [OFFENE_PUNKTE.md](OFFENE_PUNKTE.md))
* **Implementierungssprache:** Englisch (siehe [CODING_RICHTLINIEN.md](CODING_RICHTLINIEN.md))

## Modul-Struktur
```text
allergydetective/
├── shared/
│   ├── commonMain/      ← fast der gesamte Code
│   │   ├── ui/          Compose-Screens (Dashboard, Essen, Symptom, Tages-Abschluss, Ergebnis)
│   │   ├── domain/      Analyse-Engine (reines Kotlin, Unit-Tests)
│   │   ├── data/        Room-DB, DAOs, Repositories, Seed-Daten (Zutaten, Symptome)
│   │   └── platform/    Schnittstellen (expect): Speech, Billing, Notifications
│   ├── androidMain/     ← Android-Umsetzung der Schnittstellen
│   └── iosMain/         ← später: iOS-Umsetzung
├── androidApp/          Android-Einstieg (Activity, Manifest)
└── iosApp/              später: Xcode-Projekt (Einstieg)
```

## Plattform-Schnittstellen

| Schnittstelle | Android (jetzt) | iOS (später) |
| :--- | :--- | :--- |
| `SpeechInput` | SpeechRecognizer (`EXTRA_PREFER_OFFLINE`) | Speech Framework (on-device) |
| `Billing` | Google Play Billing | StoreKit |
| `Reminder` | Notifications + WorkManager | UserNotifications |
| `BackupPolicy` | `allowBackup="false"` | iCloud-Backup für DB-Datei ausschließen |

Regeln für die Schnittstellen:
* Im MVP darf die Implementierung einfach sein. Die **Schnittstelle** muss aber in `commonMain` liegen.
* Analyse-Engine und Datenbank enthalten **keinen** plattformspezifischen Code.

## Später: iOS-Build ohne eigenen Mac
* iOS-Apps lassen sich nur auf macOS mit Xcode bauen.
* Möglichkeiten:
    * **GitHub Actions** (macOS-Runner) oder **Codemagic**: Build und Upload zu TestFlight, Entwicklung weiter unter Windows
    * Mac in der Cloud mieten
    * gebrauchten Mac mini kaufen
* Kosten:
    * Apple Developer Account: 99 $/Jahr
    * Google Play: 25 $ einmalig

## Bewusst nicht im MVP
* iOS-Build
* Desktop (Windows/Mac)
* Synchronisation zwischen Geräten
