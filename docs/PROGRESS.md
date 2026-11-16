# Fortschritt: Allergie-Detektiv

**Aktuelle Phase:** Konzept ✅ → Setup ✅ → Phase 1: Daten-Herz ⏳
**Letztes Update:** 16.11.2026
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

## Phase 1: Daten-Herz (Engine)
- [x] Room-Schema laut TRD 1.x (Room 3.0.3, 18 Tabellen, Schema-Export v1, i18n-Keys)
- [x] Seed-Daten: 12 Kategorien, 26 Trigger mit Fenstern, 112 Zutaten, 24 Symptome, 4 Lifestyle-Faktoren (JSON in `composeResources/files/seed`, Anzeigenamen EN/DE, `DatabaseSeeder`)
- [ ] Seeder beim App-Start aufrufen (mit DI/Koin)
- [ ] Analyse-Engine: Episoden → Fenster → Lift → Evidenzstufe
- [ ] Unit-Tests mit synthetischen Daten

## Phase 2: Skelett (funktionales UI)
- [ ] Essen loggen, Symptom melden, Tages-Abschluss
- [ ] Notfall-Hinweis
- [ ] Ergebnis-Screen (einfach)

## Phase 3: Design & Content
- [ ] Farben, Icons, Dark Mode
- [ ] Pool mit 30–50 Motivations-Sprüchen

## Phase 4: Self-Test
- [ ] 7+ Tage selbst nutzen, Schwellenwerte kalibrieren

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
