# Fortschritt: Allergie-Detektiv

**Aktuelle Phase:** Konzept ✅ → Setup ✅ → Phase 1: Daten-Herz ✅ → Phase 2: Skelett ✅ → Phase 3: Design ✅ (auf `develop`) → Phase 4: Self-Test ⏳
**Letztes Update:** 17.11.2026
**Zeiterfassung:** [ZEITPROTOKOLL.md](ZEITPROTOKOLL.md)

---

## Phase 0: Konzept & Dokumentation ✅
- [x] PRD, TRD, UX/UI, Symptom-Katalog aus früheren Notizen sortiert und ins Repo gelegt
- [x] Review: Schwachstellen und Optimierungen ([REVIEW_Analyse.md](REVIEW_Analyse.md))
- [x] Review-Ergebnisse eingearbeitet (PRD 2.2, TRD 1.4, UX 1.3, Symptome 1.1)
- [x] Zutaten-Startliste mit Trigger-Tags ([CONTENT_Zutaten.md](CONTENT_Zutaten.md))
- [x] Architektur festgelegt: KMP, Ziel Android + iOS ([ARCHITEKTUR.md](ARCHITEKTUR.md))
- [x] IDE festgelegt: Android Studio Rabbit 2
- [x] Entscheidungen dokumentiert ([ENTSCHEIDUNGEN.md](ENTSCHEIDUNGEN.md))

## Phase 0.5: Setup ✅
- [x] Git-Repo initialisiert, Commit gemacht und auf GitHub gepusht (privat, `naimafa/allergydetective`, Branch `main`)
- [x] Android Studio Rabbit 2 installiert
- [x] Kotlin-Multiplatform-Plugin installiert
- [x] Testgerät eingerichtet (Emulator oder Handy)
- [x] KMP-Projekt angelegt (Ziele Android + iOS) in `D:\workspace\allergydetective` (Package `com.nfar.allergydetective`), `docs/` erhalten
- [x] "Hello World" läuft auf Android (Emulator und eigenes Handy, Android 13)

## Phase 1: Daten-Herz (Engine) ✅
- [x] Room-Schema laut TRD 1.x (Room 3.0.3, 18 Tabellen, Schema-Export v1, i18n-Keys)
- [x] Seed-Daten: 12 Kategorien, 26 Trigger mit Fenstern, 112 Zutaten, 24 Symptome, 3 Lifestyle-Faktoren (JSON in `composeResources/files/seed`, Anzeigenamen EN/DE, `DatabaseSeeder`)
- [x] Seeder beim App-Start aufrufen (Koin 4.2.2, `di/AppModule.kt`, `AppStartup`; im Emulator geprüft)
- [x] Analyse-Engine als reine Funktionen in `domain/analysis`: gültige Tage → Episoden → Treffer pro Fenster → Lift → Evidenzstufe → Sicher-Liste, Schwellenwerte in `AnalysisConfig`
- [x] Unit-Tests mit synthetischen Daten (35 Tests in `commonTest`, test-first)
- [x] Gruppierung nicht unterscheidbarer Kandidaten (TRD 2.7): `AnalysisResult.groups`, Schwellenwerte in `GroupingThresholds`
- [x] Repository: Room-Daten → `AnalysisInput` (`AnalysisDao`, `AnalysisRepository`, `RoomAnalysisRepository`, in Koin registriert; im Emulator mit Test-Tagebuch geprüft)

## Phase 2: Skelett (funktionales UI) ✅
- [x] Grundgerüst: Navigation (Navigation Compose), Theme mit Petrol/Salbei/Koralle, `AppClock`, Texte EN/DE
- [x] Essen loggen: Zeit-Chips, zuletzt gegessen, lokale Suche (Synonyme, Umlaute, Tippfehler), Portion S/M/L, Hinweis bei bekannter Allergie, Speichern mit Rückgängig
- [x] Symptom melden: Grid nach Gruppen, häufigste oben, Overlay (Stärke 1–5, Zeit, Notiz), Schnell-Log per langem Drücken, Rückgängig
- [x] Notfall-Hinweis vor Schwellung und Atemnot, mit Notruf-Button (112)
- [x] Tages-Abschluss: Wohlbefinden, Schlaf, Stress, Tages-Faktoren, Status; bis 2 Tage nachtragen; "Keine Beschwerden" gesperrt, wenn Symptome eingetragen sind
- [x] Home: Fortschritt "Tag X von 7" aus bestätigten Tagen
- [x] Ergebnis-Screen (einfach): Spur/Verdacht/Starker Verdacht mit Begründung, nicht unterscheidbare Gruppen, gut vertragen, Allergie-Hinweis, Disclaimer
- [x] Offene Punkte: Auto-Backup aus, freie Zeitwahl, Favoriten, eigene Zutaten, Onboarding, Tab-Bar mit Historie/Favoriten/Profil, Abend-Reminder, Vergleich im Ergebnis, Notruf je Region, Zahlenformat, Spracheingabe (Rest siehe OFFENE_PUNKTE.md)

## Phase 3: Design & Content ✅
- [x] Farben (Petrol/Salbei/Koralle), Dark Mode, Typografie, Formen, Material-Icons
- [x] Home: Tagesspruch, Fortschrittsring, farbige Kacheln; Symptom-Kacheln mit Symbolen und Gruppenfarben; Ergebnis-Karten nach Stufe; Gesichter/Sterne im Tages-Abschluss; Haptik beim Speichern
- [ ] Schrift Inter (braucht eine Font-Datei im Projekt; aktuell System-Schrift)
- [x] Pool mit 40 Tagessprüchen (EN/DE), wechselt täglich

## Phase 4: Self-Test
- [x] Vorbereitung: JSON-Export im Profil, Anleitung [SELFTEST.md](SELFTEST.md), Exporte per `.gitignore` geschützt
- [ ] App auf dem eigenen Handy installieren (`develop`)
- [ ] 7+ Tage selbst nutzen, Notizen führen (SELFTEST.md, Kapitel 3)
- [ ] Export + Notizen auswerten, Schwellenwerte in `AnalysisConfig` kalibrieren

## Später
- [ ] Zeitfenster und Trigger fachlich prüfen
- [ ] Verschlüsselung entscheiden
- [ ] iOS-Build per CI
- [ ] Store-Text anpassen, MDR-Einordnung prüfen

---

## Log
| Datum | Was |
| :--- | :--- |
| 14.11.2026 | Konzeptphase abgeschlossen: Doku sortiert, Review, Überarbeitung, Architektur- und IDE-Entscheidung |
| 14.11.2026 | Setup abgeschlossen: Android Studio, KMP-Projekt angelegt, Hello World läuft auf Android |
| 15.11.2026 | Git-Repo auf GitHub gepusht (privat). Launch-Checkliste in OFFENE_PUNKTE.md ergänzt |
| 15.11.2026 | Room-Schema v1 angelegt, Build grün, gepusht |
| 15.11.2026 | Zeitprotokoll angelegt (Timeline und Dauer je Arbeitsblock) |
| 15.11.2026 | minSdk auf 26 gesenkt, App läuft auf eigenem Handy |
| 16.11.2026 | Seed-Daten als JSON + EN/DE-Strings, Parser/Mapper/SeedDao/DatabaseSeeder, Unit-Tests |
| 16.11.2026 | Analyse-Engine test-first (Branch `feature/analysis-engine`), Annahmen in OFFENE_PUNKTE.md |
| 16.11.2026 | Engine nach `main` gemergt (PR #1). Gruppierung TRD 2.7 test-first (Branch `feature/candidate-grouping`), Gradle-Tests lokal grün |
| 16.11.2026 | Gruppierung nach `main` gemergt (PR #2). Koin eingebunden, Seeder läuft beim App-Start (Branch `feature/koin-seeder`). Im Emulator geprüft: 12 Kategorien, 26 Trigger, 112 Zutaten, 24 Symptome, 3 Lifestyle-Faktoren; zweiter Start fügt nichts doppelt ein. iOS-Teil ungebaut (kein Mac) |
| 16.11.2026 | Koin/Seeder nach `main` gemergt (PR #3). Repository Room → `AnalysisInput` (Branch `feature/analysis-repository`), 56 Unit-Tests grün. Im Emulator geprüft: 21-Tage-Test-Tagebuch in der echten Datenbank → Engine meldet Milcheiweiß, Laktose und Kuhmilch als Gruppe. **Phase 1 abgeschlossen** |
| 17.11.2026 | Phase 2 auf `develop`: je Schritt ein Branch (`feature/app-shell`, `log-meal`, `log-symptom`, `day-check`, `results-screen`), lokal per `--no-ff` gemergt (keine GitHub-PRs, siehe OFFENE_PUNKTE.md). 91 Unit-Tests grün, jeder Screen im Emulator geprüft. **Phase 2 abgeschlossen** |
| 17.11.2026 | Offene Phase-2-Punkte auf `develop` (10 Branches, je lokal gemergt). 128 Unit-Tests grün, jeder Schritt im Emulator geprüft; Spracheingabe nur bis zum System-Dialog (Emulator ohne Erkennung) |
| 17.11.2026 | Phase 3 auf `develop` (Branches `design-theme`, `home-dashboard`, `symptom-tiles`, `design-polish`): neues Farbschema hell/dunkel, Icons, Dashboard mit Tagesspruch, Symptom-Symbole, Ergebnis-Karten. 130 Unit-Tests grün, im Emulator hell und dunkel geprüft. **Phase 3 abgeschlossen** |
| 17.11.2026 | Phase 4 vorbereitet: JSON-Export für die Kalibrierung (`feature/data-export`), Anleitung SELFTEST.md. Installation aufs Handy steht aus (Handy nicht verbunden) |
