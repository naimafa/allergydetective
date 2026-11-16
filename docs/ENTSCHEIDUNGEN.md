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
| Negative Daten | Tages-Abschluss mit Bestätigung "Keine Beschwerden / Alles eingetragen"; nur bestätigte Tage zählen | TRD 3.3, UX 3.4 |
| Lifestyle | Schlaf, Stress, Sport, Zyklus, Medikament als **eigene Kandidaten** im Ranking (kein 30-%-Abzug). Alkohol nur als Trigger der Getränke, nicht als Faktor (16.11.2026) | TRD 2.5 |
| Bekannte Allergien | nicht im Ranking, nur Hinweis beim Loggen | TRD 2.6 |
| Ergebnis | Evidenzstufen Spur / Verdacht / Starker Verdacht statt Prozent | TRD 2.8 |
| Intensität | **1–5** (bestätigt), Stress 1–5 | Symptome |
| Portion | S / M / L = Faktor 0,5 / 1 / 2, erklärt per Hand-Maß | TRD 1.4 |
| Notfall | Atemnot / Schwellung → Hinweis mit Notruf 112 | Symptome Kap. 3 |
| Farben | Pastell für Flächen, dunkle Varianten für Text (WCAG ≥ 4,5:1) | UX 1.1 |

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
* Verschlüsselung (SQLCipher), verschlüsselter Export/Import, App-Sperre
* App-Name
* Premium-Idee "Detektiv-Experimente"
* Desktop-Version, Geräte-Sync

→ [OFFENE_PUNKTE.md](OFFENE_PUNKTE.md)
