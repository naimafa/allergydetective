# 🕵️‍♂️ Allergie-Detektiv

Eine diskrete, **Local-First** Tagebuch-App, die versteckte Auslöser für Allergien und Unverträglichkeiten findet – durch mathematische Korrelation von Ernährung, Symptomen und Lifestyle-Faktoren (Schlaf, Stress). Keine Cloud, kein Account, keine KI-Kosten.

## Kernideen
- **5-Sekunden-Regel:** Jede Eingabe blitzschnell (Favoriten, Voice-In, One-Tap-Symptome).
- **Zeitfenster-Analyse:** T1 Sofort (0–2h), T2 Intermediär (2–6h), T3 Spät (6–48h).
- **Störfaktor-Bereinigung:** Schlechter Schlaf / hoher Stress senkt die "Schuld" von Lebensmitteln.
- **Aha-Moment nach 7 Tagen:** Erste Analyse gratis, danach Premium-Abo (Detektiv-Pro).
- **100 % lokal:** SQLite auf dem Gerät, keine Datenweitergabe.

## Dokumentation
| Dokument | Inhalt |
| :--- | :--- |
| [docs/PRD_Vision.md](docs/PRD_Vision.md) | Produkt-Vision, Features, Monetarisierung, Roadmap |
| [docs/TRD_Technik.md](docs/TRD_Technik.md) | Datenbank-Architektur & Analyse-Algorithmus |
| [docs/UX_UI_Prototyp.md](docs/UX_UI_Prototyp.md) | Design-Sprache, Screens, User Flows |
| [docs/CONTENT_Symptome.md](docs/CONTENT_Symptome.md) | Symptom-Katalog mit IDs & Eingabe-Overlay |
| [docs/ROADMAP_Entwicklung.md](docs/ROADMAP_Entwicklung.md) | Bau-Fahrplan in 4 Phasen (Checklisten) |
| [docs/OFFENE_PUNKTE.md](docs/OFFENE_PUNKTE.md) | Widersprüche & Lücken, vor dem Coden klären |
| [docs/marketing/STORE_Beschreibung.md](docs/marketing/STORE_Beschreibung.md) | Entwurf App-Store-Text |
| [docs/workflow/KI_Coding_Workflow.md](docs/workflow/KI_Coding_Workflow.md) | Prompt-Strategie für Gemini in Android Studio |

## Tech-Stack (geplant)
- Android, Kotlin, Jetpack Compose
- Room (SQLite) für lokale Daten
- System-APIs für Speech-to-Text und In-App-Käufe

## Status
📝 Konzeptphase abgeschlossen – als Nächstes: Phase 1 „Daten-Herz“ (siehe Roadmap).

## Hinweis
Der Allergie-Detektiv ist ein Assistenz-System zur Selbstbeobachtung und kein Ersatz für eine medizinische Diagnose.
