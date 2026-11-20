# Entscheidungs-Log & Zusammenfassung Konzeptphase

Zusammenfassung der Konzept-Sitzung vom **14.11.2026**. Details stehen in den verlinkten Dokumenten. Diese Datei ist der schnelle Einstieg für neue Chats (Claude, Gemini).

---

## 1. Produkt
* **App:** Allergie-Detektiv. Local-First Tagebuch zur **Selbstbeobachtung**: Ernährung, Symptome, Schlaf und Stress → Muster in den eigenen Einträgen.
* **Zweckbestimmung:** keine Diagnose. Wortwahl "Spur / Verdacht / gut vertragen", nie "Ursache gefunden" oder "Allergie erkannt" (MDR-Risiko). → [PRD](PRD_Vision.md) 1.1
* **Monetarisierung:** erste Analyse nach 7 bestätigten Tagen gratis, danach Abo (fortlaufende Analyse, PDF für den Arzt).

## 2. Fachliche & algorithmische Entscheidungen
| Thema | Entscheidung | Referenz |
| :--- | :--- | :--- |
| Analyse-Methode | Vergleich "mit vs. ohne" (Lift mit Laplace-Glättung) statt Summen-Score | TRD 2.4 |
| Zutaten | Vorbefüllte Liste (~100) mit Trigger-Tags, 12 Basis-Kategorien | [CONTENT_Zutaten](CONTENT_Zutaten.md) |
| Zeitfenster | pro Trigger, überlappend (z.B. Histamin 0–4h, Laktose 0,25–6h, Gluten 1–48h). **Vorläufig, vor Release prüfen** | Zutaten Kap. 2 |
| Plausibilität | Trigger ↔ Symptomgruppe (A Magen, B Haut, C Kopf/Kreislauf, D Atemwege/Mund) | TRD 2.3 |
| Negative Daten | Tagesabschluss; nur bestätigte Tage zählen. Seit 19.11.2026: Angaben werden sofort gespeichert (Tag bleibt offen), ein Button "Tag abschließen": mit Symptomen → "alles eingetragen", ohne → Rückfrage "keine Beschwerden?". "Noch nachtragen" entfällt; offene Tage zeigen Startkachel ("Offen"), Erinnerung und Historie | TRD 3.3, UX 3.4 |
| Lifestyle | Schlaf, Stress, Sport, Zyklus, Medikament als **eigene Kandidaten** im Ranking (kein 30-%-Abzug). Alkohol nur als Trigger der Getränke, nicht als Faktor (16.11.2026) | TRD 2.5 |
| Roh / gekocht | Pflichtangabe nur bei Zutaten mit hitzeempfindlichem Trigger (Birkenpollen-Kreuzreaktion). Gekocht = dieser Trigger zählt nicht, andere bleiben (z.B. Sellerie-Allergen). Auch in Gerichten gespeichert (18.11.2026, Self-Test) | `HEAT_LABILE_PROPERTY_KEYS` |
| Doppelte Zutaten | Beim Start: eigene Zutat mit dem Namen einer mitgelieferten wird mit ihr zusammengeführt (Mahlzeiten und Gerichte zeigen dann auf die mitgelieferte). Beim Anlegen: gleicher Name oder Synonym gesperrt. Ähnlich (Tippfehler, Teil eines Namens): einmal nachfragen. Grund: Doppelte verteilen die Einträge und schwächen die Analyse (18.11.2026) | `CustomIngredientViewModel` |
| Kranke Tage | Faktor "Krank (z.B. Infekt, Erkältung)" im Tages-Abschluss oder Krankheitszeit von–bis / "noch krank" (Profil, Tages-Abschluss, Karte auf der Startseite mit "Wieder gesund" = gestern letzter kranker Tag). Kein eigener Kandidat (18.11.2026, Self-Test). Seit 19.11.2026 (Variante B): Kranke Tage zählen **gewichtet** statt gar nicht: Mahlzeiten und Treffer mit 0,5. Pro Symptom fragt die App "Gehört das zur Krankheit?" (Ja / Nein / Weiß nicht, auch nachträglich in der Historie): Ja = zählt nicht, Nein = 0,5 (Tagesgewicht), Weiß nicht / unbeantwortet = 0,25. Lifestyle-Kandidaten lassen kranke Tage weg. **Gewichte vorläufig, mit Self-Test kalibrieren** | `AnalysisConfig.sickness`, `AnalysisEngine` |
| Bekannte Allergien | nicht im Ranking, nur Hinweis beim Loggen | TRD 2.6 |
| Ergebnis | Evidenzstufen Spur / Verdacht / Starker Verdacht statt Prozent | TRD 2.8 |
| Intensität | **1–5** (bestätigt), Stress 1–5 | Symptome |
| Portion | S / M / L = Faktor 0,5 / 1 / 2, erklärt per Hand-Maß | TRD 1.4 |
| Mahlzeit statt Uhrzeit | Frühstück = 8:00, Mittagessen = 12:30, Abendessen = 19:00, Snack = jetzt; nie in der Zukunft (18.11.2026, Self-Test). Seit 19.11.2026: **keine Vorauswahl** (führte zu falschen Einträgen), Speichern fragt nach der Mahlzeit; unter der Auswahl steht für alle gleich "Heute, 19:00 · Ändern" (Tag und Uhrzeit änderbar, ersetzt "Andere Zeit …"). **Vorläufig, mit Self-Test prüfen** | `domain/time/DayParts.kt` |
| Symptom nach Tageszeit | Morgens = 7:00 (vor dem Frühstück), Mittags = 13:00, Nachmittags = 16:00, Abends = 20:00, Ganzer Tag = 7:00, Nach dem Essen = letzte Mahlzeit von heute + 30 min. Nur Tageszeiten, die schon begonnen haben. Lange drücken speichert weiter mit Intensität 3 und "jetzt" (18.11.2026). **Vorläufig** | `domain/time/DayParts.kt` |
| Tag wählen | Einheitliche Tageszeile in Essen, Symptome und Tagesabschluss: Heute · Gestern · Vorgestern · 📅 Datum (Kalender). Nachtragen und Tagesabschluss bis 5 Tage zurück. Tageszeiten (Morgens …) gelten für den gewählten Tag, "Jetzt" und "Nach dem Essen" nur heute; der Zeit-Dialog fragt nur noch die Uhrzeit (19.11.2026) | `ui/common/DayPicker.kt` |
| Notfall | Atemnot / Schwellung → Hinweis mit Notruf 112 | Symptome Kap. 3 |
| Farben | Pastell für Flächen, dunkle Varianten für Text (WCAG ≥ 4,5:1) | UX 1.1 |
| Designsystem | Verbindliche Palette (19.11.2026): Warmweiß `#F6F7F3` Hintergrund, Salbei `#DCE8DF` Essen, Apricot `#F6E4DC` Symptome, Lavendel `#E8E2F0` Tagesabschluss, Graugrün `#E5EBE7` Auswertung, Waldgrün `#385B50` einziger Akzent (Buttons, aktive Navigation, wichtige Icons), Text `#293831`. Sekundärtext `#5F6A63` statt `#818A82` (nur ~3,3:1). Verteilung ca. 60 % Warmweiß, 25 % Pastell, 10 % Salbei/Graugrün, 5 % Waldgrün | `ui/theme/Theme.kt` |

## 3. Technik
* **Zielplattformen:** Android + iOS aus **einer Codebasis**.
* **Aktuell:** nur Windows-Entwicklung, Tests nur für Android. → [ARCHITEKTUR](ARCHITEKTUR.md)
* **Stack:** Kotlin Multiplatform + Compose Multiplatform, Room (KMP)
* **Projekt:** Ordner `D:\workspace\allergydetective`, Package `com.nfar.allergydetective` (vorläufig).
* **Implementierungssprache:** Englisch (Code, Kommentare, Tests, Commits, DB-/JSON-Schlüssel). Doku darf Deutsch sein.
* **App-Sprachen (i18n):** Englisch (Fallback), Deutsch, Französisch, Arabisch (RTL). Texte nur in Ressourcen, die Datenbank enthält nur englische Schlüssel. → [CODING_RICHTLINIEN](CODING_RICHTLINIEN.md) Kap. 6
* **Regel:** Plattformspezifisches nur hinter Schnittstellen in `androidMain` / `iosMain`.
* **iOS später:** Build über GitHub Actions (macOS) oder Codemagic → TestFlight. Kosten: Apple 99 $/Jahr, Google Play 25 $ einmalig.
* **IDE:** **Android Studio Rabbit 2** (2026.2.2, aktuell stabil) + Kotlin-Multiplatform-Plugin.
    * IntelliJ bleibt für Spring Boot.
    * Einstellungen lassen sich importieren.
    * Die Plattformunabhängigkeit kommt vom Projekt, nicht von der IDE.

## 4. Lizenz & KI-Werkzeuge
* **Android-SDK-Lizenz:** kostenlos, auch kommerziell.
    * Die App gehört dir.
    * Nutzungsstatistik nur mit Zustimmung (beim ersten Start ablehnbar).
    * Pflichten: Datenschutz der Nutzer, Haftung liegt beim Entwickler.
    * Gerichtsstand Kalifornien.
    * Keine Rechtsberatung.
* **KI-Assistenten:**
    * Gemini in Android Studio hat eigene Bedingungen, Code geht in die Cloud.
    * Alternativen: lokales Modell (Gemma 4) oder eigener Agent per BYOA/ACP (z.B. Claude).
    * Sensible Logik-Dokumente nur bewusst teilen.
* **Workflow:** siehe [workflow/KI_Coding_Workflow.md](workflow/KI_Coding_Workflow.md). Pro Prompt nur das nötige Dokument mitgeben, bei Code-Prompts immer `ARCHITEKTUR.md`.

## 5. Bewusst vertagt
* Verschlüsselter Export/Import, App-Sperre (die Datenbank selbst ist seit 20.11.2026 auf Android verschlüsselt)
* App-Name
* Premium-Idee "Detektiv-Experimente"
* Desktop-Version, Geräte-Sync

→ [OFFENE_PUNKTE.md](OFFENE_PUNKTE.md)
