# Fortschritt: Allergie-Detektiv

**Aktuelle Phase:** Konzept ✅ → Setup ✅ → Phase 1: Daten-Herz ✅ → Phase 2: Skelett ✅ → Phase 3: Design ✅ → Phase 4: Self-Test ⏳ (alles auf `main`, PR #5)
**Letztes Update:** 26.11.2026
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
- [x] Seed-Daten: 13 Kategorien, 26 Trigger mit Fenstern, 122 Zutaten, 24 Symptome, 4 Lifestyle-Faktoren (JSON in `composeResources/files/seed`, Anzeigenamen EN/DE, `DatabaseSeeder`)
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
- [x] Schriften Inter (Text) und Playfair Display (Startseite), beide SIL OFL, seit 21.11.
- [x] Pool mit 40 Tagessprüchen (EN/DE), wechselt täglich

## Phase 4: Self-Test
- [x] Vorbereitung: JSON-Export im Profil, Anleitung [SELFTEST.md](SELFTEST.md), Exporte per `.gitignore` geschützt
- [x] App auf dem eigenen Handy installiert (`develop`, 17.11.2026)
- [x] Erste Self-Test-Rückmeldungen (18.11.2026) umgesetzt: 14 Notizen plus Nachträge, Details in [OFFENE_PUNKTE.md](OFFENE_PUNKTE.md) „Self-Test-Feedback“ und [ENTSCHEIDUNGEN.md](ENTSCHEIDUNGEN.md)
- [x] Datenbank bis Version 6 (Migrationen für bestehende Installationen, je im Emulator und auf dem Handy geprüft)
- [x] APK für weitere Testgeräte unter `D:\workspace\allergydetective-apk`
- [x] Rückmeldungen vom 19.11.2026 (PRs #7–#26): Waldgrün-Design, Startseite neu, Essen in drei Schritten, Mahlzeiten und Beschwerden bearbeiten und nachtragen, Historie als Zeilen mit Filtern, Tagesabschluss mit sofortigem Speichern, Krankheitstage gewichtet (Migration 6 → 7), Dark Mode automatisch, eine Tageszeile in allen Formularen
- [x] Rückmeldungen vom 20.11.2026 (PRs #27–#39; #27–#33 am 20.11. gemergt, #34–#39 offen): Getränk als eigene Art, Dauer beim Symptom, Bericht für den Arzt als PDF, Datenbank verschlüsselt (Android), neues App-Icon und Name „Allergy Detective“, Allergien zuklappbar, Legenden der Skalen, Korrekturen bei Schriftgröße 125 %
- [x] Testnotizen vom 21. und 22.11.2026 (PRs #44–#60, offen): App-Icon aus dem Lupen-Bild, Essen-Maske entzerrt, Synonyme in der Suche, Verlauf neueste zuerst, Profil in Karten, Einführung zum Wischen, Datenschutz-Seite, Gewürze (Migration 9 → 10), Gerichte sortieren (10 → 11), Export als PDF oder Excel, Sprachen Deutsch/Englisch/Französisch/Arabisch, Stil der Startseite auf allen Masken mit einer Schriftskala (PR #60)
- [x] Datenbank bis Version 9 (8: Art der Mahlzeit, 9: Dauer); Umstellung auf Verschlüsselung auf dem Handy mit echten Daten geprüft
- [x] Klickbarer HTML-Prototyp [docs/prototype](prototype/allergy-detective-prototype.html), Screen für Screen mit dem Handy abgeglichen
- [x] Testnotizen und Mustererkennung vom 24.11.2026 (PRs #79–#85, offen): Gerichte mit eigener Portion als Momentaufnahme (Migration 12 → 13), eine Schriftskala für alle Formulare, Mustererkennung nach Tagen mit gelernter Verzögerung und schnellen Reaktionen, Konzept in [KONZEPT_MUSTERERKENNUNG.md](KONZEPT_MUSTERERKENNUNG.md)
- [x] Testnotizen und Katalog vom 25.11.2026 (PRs #87–#100, offen): nach dem Speichern zum Dashboard, Symptom-Dauer feiner, Gewürze in Prise/Löffel, „Warum noch kein Muster?“, Symptom-Bereiche verschiebbar und als Karten, Ziehen erst nach langem Drücken, „Meine Zutaten“ (bearbeiten, löschen, durch Zutat oder Gericht ersetzen), Katalog 150 → 270 Zutaten mit drei Getreide-Kategorien, Mehlen, Süßungsmitteln und Trockenobst (Migrationen 13 → 16), Auswahl „normal / glutenfrei“, Lückenliste in [KATALOG_LUECKEN.md](KATALOG_LUECKEN.md)
- [x] Weglass-Test vom 25.11.2026 abends (PRs #102–#106, offen): ein Lebensmittel weglassen, Start an einem der letzten 14 Tage, mittlere Stärke pro Tag „vorher / ohne“ als Tabelle; dazu zwei Fixes (weitere Zutat, Grund „kein Muster“ im Bericht). Offen: Handytest
- [ ] 7+ Tage selbst nutzen, Notizen führen (SELFTEST.md, Kapitel 3)
- [ ] Export + Notizen auswerten, Schwellenwerte in `AnalysisConfig` kalibrieren

## Später
- [ ] Zeitfenster und Trigger fachlich prüfen
- [x] Verschlüsselung: SQLCipher + Android Keystore (20.11.2026)
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
| 18.11.2026 | Self-Test-Rückmeldungen auf `develop` (19 Branches): 4 Bugs (Speichern-Button, Export als Datei, Tages-Abschluss wählt heute, Dinkelvollkornbrot), Öle & Oliven, Mahlzeit und Tageszeit statt Uhrzeit, kranke Tage und Krankheitszeiträume, roh/gekocht als Pflicht, Gerichte anlegen/bearbeiten, neue Zutat nur „Was ist drin?“, Doppelt-Prüfung und Zusammenführen, Einführung (auch im Profil), Historie und Kacheln neu gestaltet. DB-Migrationen 1 → 6. 157 Unit-Tests grün, jeder Schritt im Emulator geprüft und aufs Handy installiert |
| 18.11.2026 | PC-Absturz: Git-Metadaten repariert, nichts verloren. `gh` eingerichtet, `develop` per PR #5 nach `main` gemergt (erster PR seit Phase 1) |
| 19.11.2026 | 20 PRs nach `develop` (#7–#26), Design-Rückmeldungen aus dem Handy-Test: Waldgrün-Palette, Startseite und Kacheln, Essen in drei Schritten, Historie mit Filtern, Zeilen und Detail-Blatt, Bearbeiten und Nachtragen, Tagesabschluss mit Autosave, Krankheitstage gewichtet (Migration 6 → 7), Dark Mode (Einstellung und automatisch), gleiche Tageszeile überall. #7–#17 am selben Abend gemergt, #18–#26 am 20.11. (nachträglich aus der Git-Historie eingetragen) |
| 20.11.2026 | 13 PRs nach `develop` (#27–#39), je ein Branch, aufeinander aufbauend: Profil-Reihenfolge, Getränk (Migration 7 → 8), Icon, Dauer (8 → 9), Arzt-PDF, SQLCipher-Verschlüsselung mit Keystore, Icon aus dem Entwurf, zuklappbare Allergien, Skalen-Legenden, HTML-Prototyp, drei Korrekturen aus dem Handy-Abgleich. Tests grün, Stand auf dem Handy installiert; Verschlüsselung dort mit echten Daten geprüft |
| 21.11.2026 | Startseite nach dem Design-Bild neu: PR #41 (hell: gemessene Farben, Playfair Display und Inter, eigenes Blätter-Bild als Hintergrund, Logo per Code, Kacheln ohne Scrollen, „Verlauf“ statt „Historie“) und PR #42 (Dark Mode: Waldgrün, derselbe Zweig neu eingefärbt, klare Kacheln). Aufeinander aufbauend auf #40, auf dem Handy hell und dunkel geprüft, Prototyp angeglichen, Tests grün |
| 21.11.2026 | App-Icon aus dem Lupen-Bild (PR #44), Essen-Maske (PR #45), Testnotizen Teil 2 (PRs #46–#54): Synonyme, Favoriten, Verlauf neueste zuerst, „gut vertragen“ erklärt, Profil-Karten, krank → Symptome, Einführung zum Wischen, Datenschutz-Seite. Je auf dem Handy geprüft |
| 22.11.2026 | Gewürze (Migration 9 → 10, PR #55), Gerichte per Finger sortieren (10 → 11, PR #56), Bericht/Export als PDF oder Excel ohne JSON (PR #57), Sprachen DE/EN/FR/AR mit Arabisch von rechts nach links (PR #58). Migrationen mit echten Daten auf dem Handy geprüft, Tests grün; FR/AR noch von Muttersprachlern prüfen lassen |
| 22.11.2026 | Abends: Stil der Startseite auf allen Masken (PR #60): Blätter-Hintergrund, Playfair-Titel, cremefarbene Karten, keine reine weiße Farbe, Essen-Maske in Schritten, Verlauf/Favoriten/Profil umgebaut, eine Schriftskala, Auswahl-Pillen in einer Zeile. Auf dem Handy bei Schrift 125 % geprüft; Symptome, Tagesabschluss, Muster, Einführung und Datenschutz noch nicht angeschaut |
| 23.11.2026 | 17 PRs nach `develop` (#61–#77), aufeinander aufbauend: Essen-Maske nach Design-Bild (mehrere Gerichte, zusammengeführte Zutaten, Kategorie-Bilder), Testnotizen (Abwählen, Uhrzeit in Schritt 1, Suche, vorhandene Mahlzeit öffnen, Gerichte, Kräuter-Kategorie mit Migration 11 → 12, Tagesabschluss, Verlauf, Glocke, Sprache, Symptome in Schritten), Ziel im Profil mit neuem Abschnitt, Muster mit Erklärung und Maßnahmen, Bericht mit eigenem Zeitraum, lebendigere Einführung, Bezahlseite nur als versteckte Testansicht, Profil in fünf Gruppen. Tests grün, jeweils auf dem Handy geprüft; offen: Widerspruch „nicht unterscheidbar“ / „gut vertragen“ in der Auswertung |
| 24.11.2026 | 7 PRs (#79–#85), gestapelt auf develop: Testnotizen (Popups, doppeltes Frühstück, Symptome, Tagesabschluss, Excel-Tagebuch), Gerichte mit eigener Portion und Fragen bei doppelten Zutaten, Schriftskala 30/18/16/14/12 sp mit Portion-Feld „M ▾“, keine Spur ohne echte Beschwerden, Verlauf-Blatt scrollt, Mahlzeit als Momentaufnahme (Migration 12 → 13, mit echten Daten geprüft), Mustererkennung nach Tagen (Verzögerung 0–3 Tage, schnelle Reaktionen bis 2 h, „fast täglich gegessen“). Tests grün, jeweils auf dem Handy. Offen: Fass-Wert, Faktoren Medikamente/Periode, Spurensuche-Zeitleiste, Experimente |
| 25.11.2026 | 14 PRs (#87–#100), gestapelt auf develop: Testnotizen (Dashboard nach Speichern, Dauer 3–6/6–12 Std., Gewürze in Prise/Löffel, Grund bei „kein Muster“, Bilder bei Zutaten in Gerichten), Symptom-Bereiche verschiebbar (Ziehen nach langem Drücken) und als Karten, „Meine Zutaten“ mit Bearbeiten/Löschen/Ersetzen (auch durch ein Gericht), Gerichtsnamen nicht mehr als Zutat, Katalog 150 → 270 (Alltags-Zutaten, Süßungsmittel, Getreide & Mehl / Nudeln & Teigwaren / Brot & Backwaren, Trockenobst einzeln; Migrationen 13 → 16, auf dem Handy gelaufen), „normal / glutenfrei“. Tests grün. PC-Absturz, danach zwei Sitzungen im selben Ordner. Offen: Handytest der neuen Funktionen, eigene Zutaten aufräumen |
