# Fortschritt: Allergie-Detektiv

**Aktuelle Phase:** Konzept ✅ → Setup ✅ (Git offen) → Phase 1: Daten-Herz ⏳
**Letztes Update:** 14.11.2026

---

## Phase 0: Konzept & Dokumentation ✅
- [x] PRD, TRD, UX/UI, Symptom-Katalog aus früheren Notizen sortiert und ins Repo gelegt
- [x] Review: Schwachstellen und Optimierungen ([REVIEW_Analyse.md](REVIEW_Analyse.md))
- [x] Review-Ergebnisse eingearbeitet (PRD 2.2, TRD 1.4, UX 1.3, Symptome 1.1)
- [x] Zutaten-Startliste mit Trigger-Tags ([CONTENT_Zutaten.md](CONTENT_Zutaten.md))
- [x] Architektur festgelegt: KMP, Ziel Android + iOS ([ARCHITEKTUR.md](ARCHITEKTUR.md))
- [x] IDE festgelegt: Android Studio Rabbit 2
- [x] Entscheidungen dokumentiert ([ENTSCHEIDUNGEN.md](ENTSCHEIDUNGEN.md))

## Phase 0.5: Setup ✅ (Git offen)
- [ ] Git-Repo initialisieren, ersten Commit machen und auf GitHub pushen (privat)
- [x] Android Studio Rabbit 2 installiert
- [x] Kotlin-Multiplatform-Plugin installiert
- [x] Testgerät eingerichtet (Emulator oder Handy)
- [x] KMP-Projekt angelegt (Ziele Android + iOS) in `D:\workspace\allergydetective` (Package `com.nfar.allergydetective`), `docs/` erhalten
- [x] "Hello World" läuft auf Android

## Phase 1: Daten-Herz (Engine)
- [ ] Room-Schema laut TRD 1.x
- [ ] Seed-Daten: Kategorien, Trigger, Fenster, Zutaten, Symptome
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
